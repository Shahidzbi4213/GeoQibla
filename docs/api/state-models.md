# State Models

## QiblaState

```kotlin
data class QiblaState(
    val uiState: QiblaUiState = QiblaUiState.IDLE,
    val compass: QiblaCompassState = QiblaCompassState(),
    val location: QiblaLocationState = QiblaLocationState(),
    val sensorAccuracy: QiblaSensorAccuracy = QiblaSensorAccuracy.UNKNOWN,
    val orientationSource: QiblaOrientationSource = QiblaOrientationSource.NONE,
    val isStarted: Boolean = false,
    val errorMessage: String? = null,
)
```

## QiblaUiState

`QiblaUiState` values are:

- `IDLE`
- `REQUESTING_PERMISSION`
- `PERMISSION_REQUIRED`
- `PERMISSION_DENIED`
- `PERMISSION_PERMANENTLY_DENIED`
- `LOCATION_DISABLED`
- `LOCATING`
- `SENSOR_UNAVAILABLE`
- `CALIBRATION_NEEDED`
- `TILTED`
- `READY`
- `NEAR_QIBLA`
- `ALIGNED`
- `ERROR`

## QiblaCompassState

```kotlin
data class QiblaCompassState(
    val qiblaBearingDegrees: Float? = null,
    val azimuthDegrees: Float? = null,
    val directionToQiblaDegrees: Float? = null,
    val distanceToKaabaMeters: Double? = null,
    val tiltDegrees: Float = 0f,
    val magneticFieldMicrotesla: Float? = null,
    val isNearQibla: Boolean = false,
    val isAligned: Boolean = false,
    val isTilted: Boolean = false,
)
```

## QiblaLocationState

```kotlin
data class QiblaLocationState(
    val access: QiblaLocationAccess = QiblaLocationAccess.UNKNOWN,
    val fix: QiblaLocationFix? = null,
    val isLocationEnabled: Boolean = true,
    val isResolvingAddress: Boolean = false,
    val label: String? = null,
)
```

## QiblaLocationFix

```kotlin
data class QiblaLocationFix(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double? = null,
    val horizontalAccuracyMeters: Double? = null,
    val addressLabel: String? = null,
    val declinationDegrees: Float? = null,
)
```

## Enums

`QiblaLocationAccess`: `UNKNOWN`, `NOT_DETERMINED`, `GRANTED`, `DENIED`, `PERMANENTLY_DENIED`.

`QiblaSensorAccuracy`: `UNKNOWN`, `UNAVAILABLE`, `UNRELIABLE`, `LOW`, `MEDIUM`, `HIGH`.

`QiblaOrientationSource`: `NONE`, `ROTATION_VECTOR`, `ACCELEROMETER_MAGNETOMETER`, `PLATFORM_HEADING`.
