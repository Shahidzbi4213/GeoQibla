# QiblaController

```kotlin
interface QiblaController {
    val state: StateFlow<QiblaState>

    fun start()
    fun stop()
    fun retryLocation()
    fun requestPermission()
    fun openLocationSettings()
    fun openAppSettings()
    fun dismissCalibration()
}
```

`QiblaController` owns the runtime flow for location, heading, and alignment state.

## rememberQiblaController

```kotlin
@Composable
fun rememberQiblaController(
    config: QiblaConfig = QiblaConfig(),
): QiblaController
```

Creates a controller using platform services from the current composition.

## State

`state` is a `StateFlow<QiblaState>`. Collect it in Compose with `collectAsState()` when building custom UI.

## Actions

| Method | Behavior |
| --- | --- |
| `start()` | Starts observing permission, location, and orientation streams. |
| `stop()` | Stops active observers and resets started state to idle. |
| `retryLocation()` | Restarts location observation when already started. |
| `requestPermission()` | Launches the platform location permission flow. |
| `openLocationSettings()` | Opens system location settings. |
| `openAppSettings()` | Opens app-specific settings. |
| `dismissCalibration()` | Hides the current calibration warning until conditions change. |
