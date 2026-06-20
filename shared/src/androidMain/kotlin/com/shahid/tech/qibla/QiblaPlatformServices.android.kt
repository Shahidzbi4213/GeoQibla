package com.shahid.tech.qibla

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

private val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

@Composable
internal actual fun rememberQiblaPlatformServices(): QiblaPlatformServices {
    val context = LocalContext.current
    val view = LocalView.current
    val appContext = remember(context) { context.applicationContext }
    val preferences = remember(appContext) {
        appContext.getSharedPreferences("geoqibla_permissions", Context.MODE_PRIVATE)
    }
    val access = remember(context, preferences) {
        MutableStateFlow(resolveLocationAccess(context, preferences))
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        access.value = if (result.values.any { it }) {
            QiblaLocationAccess.GRANTED
        } else {
            deniedAccess(context, preferences)
        }
    }

    LaunchedEffect(context) {
        access.value = resolveLocationAccess(context, preferences)
    }

    return remember(context, view, launcher, preferences, access) {
        AndroidQiblaPlatformServices(
            context = context,
            view = view,
            permissionLauncher = launcher,
            preferences = preferences,
            locationAccess = access,
        )
    }
}

internal actual fun currentTimeMillis(): Long = System.currentTimeMillis()

private class AndroidQiblaPlatformServices(
    private val context: Context,
    private val view: View,
    private val permissionLauncher: ActivityResultLauncher<Array<String>>,
    private val preferences: SharedPreferences,
    override val locationAccess: MutableStateFlow<QiblaLocationAccess>,
) : QiblaPlatformServices {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    override fun requestPermission() {
        val access = resolveLocationAccess(context, preferences)
        if (access == QiblaLocationAccess.GRANTED) {
            locationAccess.value = access
            return
        }

        preferences.edit().putBoolean(HasRequestedLocationPermissionKey, true).apply()
        permissionLauncher.launch(LocationPermissions)
    }

    @SuppressLint("MissingPermission")
    override fun observeLocation(config: QiblaConfig): Flow<QiblaLocationSnapshot> =
        callbackFlow {
            val access = resolveLocationAccess(context, preferences)
            locationAccess.value = access
            if (access != QiblaLocationAccess.GRANTED) {
                trySend(
                    QiblaLocationSnapshot(
                        access = access,
                        fix = null,
                        isLocationEnabled = isLocationEnabled(),
                    ),
                )
                awaitClose {}
                return@callbackFlow
            }

            if (!isLocationEnabled()) {
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

            bestLastKnownLocation()?.let { location ->
                trySend(location.toSnapshot())
            }

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    trySend(location.toSnapshot())
                }

                @Deprecated("Deprecated platform callback")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                override fun onProviderEnabled(provider: String) {
                    trySend(
                        QiblaLocationSnapshot(
                            access = QiblaLocationAccess.GRANTED,
                            fix = null,
                            isLocationEnabled = true,
                        ),
                    )
                }

                override fun onProviderDisabled(provider: String) {
                    trySend(
                        QiblaLocationSnapshot(
                            access = QiblaLocationAccess.GRANTED,
                            fix = null,
                            isLocationEnabled = isLocationEnabled(),
                        ),
                    )
                }
            }

            val providers = enabledProviders()
            if (providers.isEmpty()) {
                trySend(
                    QiblaLocationSnapshot(
                        access = QiblaLocationAccess.GRANTED,
                        fix = null,
                        isLocationEnabled = false,
                    ),
                )
            } else {
                providers.forEach { provider ->
                    locationManager.requestLocationUpdates(
                        provider,
                        config.locationUpdateIntervalMillis,
                        0f,
                        listener,
                        Looper.getMainLooper(),
                    )
                }
            }

            awaitClose {
                locationManager.removeUpdates(listener)
            }
        }

    override fun observeOrientation(config: QiblaConfig): Flow<QiblaOrientationSnapshot> =
        callbackFlow {
            val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            if (rotationVector == null && (accelerometer == null || magnetometer == null)) {
                trySend(
                    QiblaOrientationSnapshot(
                        azimuthDegrees = null,
                        azimuthReference = QiblaAzimuthReference.MAGNETIC_NORTH,
                        accuracy = QiblaSensorAccuracy.UNAVAILABLE,
                        source = QiblaOrientationSource.NONE,
                    ),
                )
                awaitClose {}
                return@callbackFlow
            }

            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)
            val accelerometerReading = FloatArray(3)
            val magnetometerReading = FloatArray(3)
            var hasAccelerometerReading = false
            var hasMagnetometerReading = false
            var latestAccuracy = QiblaSensorAccuracy.UNKNOWN
            var latestMagneticField: Float? = null

            fun sendOrientation(source: QiblaOrientationSource) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuth = radiansToDegrees(orientationAngles[0])
                val pitch = abs(radiansToDegrees(orientationAngles[1]))
                val roll = abs(radiansToDegrees(orientationAngles[2]))
                trySend(
                    QiblaOrientationSnapshot(
                        azimuthDegrees = QiblaMath.normalizeDegrees(azimuth),
                        azimuthReference = QiblaAzimuthReference.MAGNETIC_NORTH,
                        accuracy = latestAccuracy,
                        source = source,
                        tiltDegrees = max(pitch, roll),
                        magneticFieldMicrotesla = latestMagneticField,
                    ),
                )
            }

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                            sendOrientation(QiblaOrientationSource.ROTATION_VECTOR)
                        }

                        Sensor.TYPE_ACCELEROMETER -> {
                            System.arraycopy(
                                event.values,
                                0,
                                accelerometerReading,
                                0,
                                accelerometerReading.size,
                            )
                            hasAccelerometerReading = true
                        }

                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            System.arraycopy(
                                event.values,
                                0,
                                magnetometerReading,
                                0,
                                magnetometerReading.size,
                            )
                            latestMagneticField = magneticMagnitude(event.values)
                            hasMagnetometerReading = true
                        }
                    }

                    if (rotationVector == null && hasAccelerometerReading && hasMagnetometerReading) {
                        val hasMatrix = SensorManager.getRotationMatrix(
                            rotationMatrix,
                            null,
                            accelerometerReading,
                            magnetometerReading,
                        )
                        if (hasMatrix) {
                            sendOrientation(QiblaOrientationSource.ACCELEROMETER_MAGNETOMETER)
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                    latestAccuracy = accuracy.toQiblaAccuracy()
                }
            }

            rotationVector?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
            }
            if (rotationVector == null) {
                accelerometer?.let {
                    sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
                }
            }
            magnetometer?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
            }

            awaitClose {
                sensorManager.unregisterListener(listener)
            }
        }

    override fun openLocationSettings() {
        context.startActivity(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    override fun openAppSettings() {
        val uri = Uri.fromParts("package", context.packageName, null)
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    override fun performAlignmentHaptic() {
        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }
        view.performHapticFeedback(effect)
    }

    private fun Location.toSnapshot(): QiblaLocationSnapshot {
        val fix = QiblaLocationFix(
            latitude = latitude,
            longitude = longitude,
            altitudeMeters = altitude.takeIf { hasAltitude() },
            horizontalAccuracyMeters = accuracy.toDouble().takeIf { hasAccuracy() },
            addressLabel = "${latitude.toCoordinateLabel()}, ${longitude.toCoordinateLabel()}",
            declinationDegrees = GeomagneticField(
                latitude.toFloat(),
                longitude.toFloat(),
                altitude.toFloat(),
                time.takeIf { it > 0L } ?: System.currentTimeMillis(),
            ).declination,
        )
        return QiblaLocationSnapshot(
            access = QiblaLocationAccess.GRANTED,
            fix = fix,
            isLocationEnabled = isLocationEnabled(),
            label = fix.addressLabel,
        )
    }

    @SuppressLint("MissingPermission")
    private fun bestLastKnownLocation(): Location? =
        enabledProviders()
            .mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull { it.time }

    private fun enabledProviders(): List<String> =
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { provider ->
                runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
            }

    private fun isLocationEnabled(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            enabledProviders().isNotEmpty()
        }
}

private const val HasRequestedLocationPermissionKey = "has_requested_location_permission"

private fun resolveLocationAccess(
    context: Context,
    preferences: SharedPreferences,
): QiblaLocationAccess =
    if (hasAnyLocationPermission(context)) {
        QiblaLocationAccess.GRANTED
    } else if (!preferences.getBoolean(HasRequestedLocationPermissionKey, false)) {
        QiblaLocationAccess.NOT_DETERMINED
    } else {
        deniedAccess(context, preferences)
    }

private fun deniedAccess(
    context: Context,
    preferences: SharedPreferences,
): QiblaLocationAccess {
    val activity = context.findActivity()
    val shouldShowRationale = activity?.let { owner ->
        LocationPermissions.any { permission ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                owner.shouldShowRequestPermissionRationale(permission)
            } else {
                false
            }
        }
    } ?: false
    val hasRequested = preferences.getBoolean(HasRequestedLocationPermissionKey, false)
    return if (hasRequested && !shouldShowRationale) {
        QiblaLocationAccess.PERMANENTLY_DENIED
    } else {
        QiblaLocationAccess.DENIED
    }
}

private fun hasAnyLocationPermission(context: Context): Boolean =
    LocationPermissions.any { permission ->
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private fun Int.toQiblaAccuracy(): QiblaSensorAccuracy =
    when (this) {
        SensorManager.SENSOR_STATUS_UNRELIABLE -> QiblaSensorAccuracy.UNRELIABLE
        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> QiblaSensorAccuracy.LOW
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> QiblaSensorAccuracy.MEDIUM
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> QiblaSensorAccuracy.HIGH
        else -> QiblaSensorAccuracy.UNKNOWN
    }

private fun radiansToDegrees(value: Float): Float =
    (value * 180f / PI.toFloat())

private fun magneticMagnitude(values: FloatArray): Float =
    sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])

private fun Double.toCoordinateLabel(): String {
    val rounded = (this * 10_000.0).toInt() / 10_000.0
    return rounded.toString()
}
