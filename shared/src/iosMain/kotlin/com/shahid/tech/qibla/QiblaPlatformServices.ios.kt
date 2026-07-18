@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.shahid.tech.qibla

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import platform.CoreLocation.*
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIDeviceOrientationDidChangeNotification
import platform.darwin.NSObject
import kotlin.math.sqrt

@Composable
internal actual fun rememberQiblaPlatformServices(): QiblaPlatformServices =
    remember { IosQiblaPlatformServices() }

internal actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1_000.0).toLong()

private class IosQiblaPlatformServices : QiblaPlatformServices {
    private val locationManager = CLLocationManager()
    private val delegate = IosQiblaLocationDelegate()

    override val locationAccess = MutableStateFlow(
        locationManager.authorizationStatus.toLocationAccess(),
    )

    init {
        delegate.onAuthorizationChanged = { status ->
            locationAccess.value = status.toLocationAccess()
        }
        locationManager.delegate = delegate
    }

    override fun requestPermission() {
        locationManager.requestWhenInUseAuthorization()
    }

    override fun observeLocation(config: QiblaConfig): Flow<QiblaLocationSnapshot> =
        callbackFlow {
            delegate.onLocation = { location ->
                trySend(location.toSnapshot())
            }
            delegate.onLocationError = onError@{ error ->
                if (error.isTransientLocationUnknown()) return@onError
                val access = locationManager.authorizationStatus.toLocationAccess()
                locationAccess.value = access
                trySend(
                    QiblaLocationSnapshot(
                        access = access,
                        fix = null,
                        isLocationEnabled = true,
                        errorMessage = error.localizedDescription,
                    ),
                )
            }

            var isUpdatingLocation = false
            val authorizationJob = launch {
                locationAccess.collect { access ->
                    if (access == QiblaLocationAccess.GRANTED) {
                        if (!isUpdatingLocation) {
                            locationManager.desiredAccuracy =
                                platform.CoreLocation.kCLLocationAccuracyBest
                            locationManager.startUpdatingLocation()
                            isUpdatingLocation = true
                        }
                    } else {
                        if (isUpdatingLocation) {
                            locationManager.stopUpdatingLocation()
                            isUpdatingLocation = false
                        }
                        trySend(
                            QiblaLocationSnapshot(
                                access = access,
                                fix = null,
                                isLocationEnabled = true,
                            ),
                        )
                    }
                }
            }

            locationAccess.value = locationManager.authorizationStatus.toLocationAccess()
            if (locationAccess.value == QiblaLocationAccess.NOT_DETERMINED) {
                locationManager.requestWhenInUseAuthorization()
            }

            awaitClose {
                authorizationJob.cancel()
                if (isUpdatingLocation) locationManager.stopUpdatingLocation()
                delegate.onLocation = null
                delegate.onLocationError = null
            }
        }

    override fun observeOrientation(config: QiblaConfig): Flow<QiblaOrientationSnapshot> =
        callbackFlow {
            if (!CLLocationManager.headingAvailable()) {
                trySend(
                    QiblaOrientationSnapshot(
                        azimuthDegrees = null,
                        azimuthReference = QiblaAzimuthReference.TRUE_NORTH,
                        accuracy = QiblaSensorAccuracy.UNAVAILABLE,
                        source = QiblaOrientationSource.NONE,
                    ),
                )
                awaitClose {}
                return@callbackFlow
            }

            delegate.onHeading = { heading ->
                trySend(heading.toSnapshot())
            }

            // CLHeading values are reported relative to headingOrientation, which
            // defaults to portrait. Keep it in sync with the physical device
            // orientation so the heading stays correct in landscape.
            val device = UIDevice.currentDevice
            device.beginGeneratingDeviceOrientationNotifications()
            device.orientation.toHeadingOrientation()?.let {
                locationManager.headingOrientation = it
            }
            val orientationObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = UIDeviceOrientationDidChangeNotification,
                `object` = null,
                queue = NSOperationQueue.mainQueue,
            ) { _ ->
                UIDevice.currentDevice.orientation.toHeadingOrientation()?.let {
                    locationManager.headingOrientation = it
                }
            }

            locationManager.startUpdatingHeading()

            awaitClose {
                locationManager.stopUpdatingHeading()
                NSNotificationCenter.defaultCenter.removeObserver(orientationObserver)
                device.endGeneratingDeviceOrientationNotifications()
                delegate.onHeading = null
            }
        }

    override fun openLocationSettings() {
        openAppSettings()
    }

    override fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }

    override fun performAlignmentHaptic() = Unit
}

private class IosQiblaLocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {
    var onAuthorizationChanged: ((CLAuthorizationStatus) -> Unit)? = null
    var onLocation: ((CLLocation) -> Unit)? = null
    var onLocationError: ((NSError) -> Unit)? = null
    var onHeading: ((CLHeading) -> Unit)? = null

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onAuthorizationChanged?.invoke(manager.authorizationStatus)
    }

    override fun locationManager(
        manager: CLLocationManager,
        didUpdateLocations: List<*>,
    ) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return
        onLocation?.invoke(location)
    }

    override fun locationManager(
        manager: CLLocationManager,
        didFailWithError: NSError,
    ) {
        onLocationError?.invoke(didFailWithError)
    }

    override fun locationManager(
        manager: CLLocationManager,
        didUpdateHeading: CLHeading,
    ) {
        onHeading?.invoke(didUpdateHeading)
    }

    override fun locationManagerShouldDisplayHeadingCalibration(
        manager: CLLocationManager,
    ): Boolean = true
}

private fun CLAuthorizationStatus.toLocationAccess(): QiblaLocationAccess =
    when (this) {
        kCLAuthorizationStatusAuthorizedAlways,
        kCLAuthorizationStatusAuthorizedWhenInUse,
        -> QiblaLocationAccess.GRANTED

        kCLAuthorizationStatusNotDetermined -> QiblaLocationAccess.NOT_DETERMINED
        kCLAuthorizationStatusDenied -> QiblaLocationAccess.PERMANENTLY_DENIED
        kCLAuthorizationStatusRestricted -> QiblaLocationAccess.DENIED
        else -> QiblaLocationAccess.UNKNOWN
    }

/**
 * Maps the physical device orientation to the matching heading reference used by
 * [CLLocationManager.headingOrientation]. Face-up/face-down and unknown
 * orientations return null so the last valid reference is preserved.
 */
private fun UIDeviceOrientation.toHeadingOrientation(): CLDeviceOrientation? =
    when (this) {
        UIDeviceOrientation.UIDeviceOrientationPortrait -> CLDeviceOrientationPortrait
        UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown ->
            CLDeviceOrientationPortraitUpsideDown

        UIDeviceOrientation.UIDeviceOrientationLandscapeLeft -> CLDeviceOrientationLandscapeLeft
        UIDeviceOrientation.UIDeviceOrientationLandscapeRight -> CLDeviceOrientationLandscapeRight
        else -> null
    }

internal fun NSError.isTransientLocationUnknown(): Boolean =
    domain == kCLErrorDomain && code == kCLErrorLocationUnknown

private fun CLLocation.toSnapshot(): QiblaLocationSnapshot {
    val coordinate = coordinate.useContents {
        latitude to longitude
    }
    val fix = QiblaLocationFix(
        latitude = coordinate.first,
        longitude = coordinate.second,
        altitudeMeters = altitude.takeIf { verticalAccuracy >= 0.0 },
        horizontalAccuracyMeters = horizontalAccuracy.takeIf { it >= 0.0 },
        addressLabel = "${coordinate.first.toCoordinateLabel()}, ${coordinate.second.toCoordinateLabel()}",
        declinationDegrees = null,
    )
    return QiblaLocationSnapshot(
        access = QiblaLocationAccess.GRANTED,
        fix = fix,
        isLocationEnabled = true,
        label = fix.addressLabel,
    )
}

private fun CLHeading.toSnapshot(): QiblaOrientationSnapshot {
    val usesTrueNorth = trueHeading >= 0.0
    val headingDegrees = if (usesTrueNorth) trueHeading else magneticHeading
    return QiblaOrientationSnapshot(
        azimuthDegrees = QiblaMath.normalizeDegrees(headingDegrees.toFloat()),
        azimuthReference = if (usesTrueNorth) {
            QiblaAzimuthReference.TRUE_NORTH
        } else {
            QiblaAzimuthReference.MAGNETIC_NORTH
        },
        accuracy = headingAccuracy.toQiblaAccuracy(),
        source = QiblaOrientationSource.PLATFORM_HEADING,
        tiltDegrees = 0f,
        magneticFieldMicrotesla = sqrt(x * x + y * y + z * z).toFloat(),
    )
}

private fun Double.toQiblaAccuracy(): QiblaSensorAccuracy =
    when {
        this < 0.0 -> QiblaSensorAccuracy.UNRELIABLE
        this <= 5.0 -> QiblaSensorAccuracy.HIGH
        this <= 15.0 -> QiblaSensorAccuracy.MEDIUM
        else -> QiblaSensorAccuracy.LOW
    }

private fun Double.toCoordinateLabel(): String {
    val rounded = (this * 10_000.0).toInt() / 10_000.0
    return rounded.toString()
}
