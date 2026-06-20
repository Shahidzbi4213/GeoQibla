@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.shahid.tech.qibla

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

@Composable
internal actual fun rememberQiblaPlatformServices(): QiblaPlatformServices =
    remember { IosQiblaPlatformServices() }

internal actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1_000.0).toLong()

private class IosQiblaPlatformServices : QiblaPlatformServices {
    private val locationManager = CLLocationManager()
    private val delegate = IosQiblaLocationDelegate()

    override val locationAccess = MutableStateFlow(currentAuthorizationStatus().toLocationAccess())

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
            val access = currentAuthorizationStatus().toLocationAccess()
            locationAccess.value = access
            if (access != QiblaLocationAccess.GRANTED) {
                trySend(
                    QiblaLocationSnapshot(
                        access = access,
                        fix = null,
                        isLocationEnabled = CLLocationManager.locationServicesEnabled(),
                    ),
                )
                awaitClose {}
                return@callbackFlow
            }

            if (!CLLocationManager.locationServicesEnabled()) {
                trySend(
                    QiblaLocationSnapshot(
                        access = QiblaLocationAccess.GRANTED,
                        fix = null,
                        isLocationEnabled = false,
                    ),
                )
                awaitClose {}
                return@callbackFlow
            }

            delegate.onLocation = { location ->
                trySend(location.toSnapshot())
            }
            delegate.onLocationError = { error ->
                trySend(
                    QiblaLocationSnapshot(
                        access = QiblaLocationAccess.GRANTED,
                        fix = null,
                        isLocationEnabled = CLLocationManager.locationServicesEnabled(),
                        errorMessage = error.localizedDescription,
                    ),
                )
            }

            locationManager.desiredAccuracy = platform.CoreLocation.kCLLocationAccuracyBest
            locationManager.startUpdatingLocation()

            awaitClose {
                locationManager.stopUpdatingLocation()
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
            locationManager.startUpdatingHeading()

            awaitClose {
                locationManager.stopUpdatingHeading()
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
        onAuthorizationChanged?.invoke(currentAuthorizationStatus())
    }

    override fun locationManager(
        manager: CLLocationManager,
        didChangeAuthorizationStatus: CLAuthorizationStatus,
    ) {
        onAuthorizationChanged?.invoke(didChangeAuthorizationStatus)
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

private fun currentAuthorizationStatus(): CLAuthorizationStatus =
    CLLocationManager.authorizationStatus()

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
        isLocationEnabled = CLLocationManager.locationServicesEnabled(),
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
