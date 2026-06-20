# Custom UI

Use `QiblaController.state` when you want your own layout.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.shahid.tech.qibla.QiblaController

@Composable
fun CustomQiblaScreen(controller: QiblaController) {
    val state by controller.state.collectAsState()

    DisposableEffect(controller) {
        controller.start()
        onDispose { controller.stop() }
    }

    // Render state.uiState, state.compass, and state.location here.
}
```

## State Shape

`QiblaState` contains:

- `uiState` for the main user-facing state.
- `compass` for Qibla bearing, current heading, direction delta, distance, tilt, and magnetic field.
- `location` for permission access, current fix, location enabled status, and display label.
- `sensorAccuracy` and `orientationSource` for diagnostics.
- `isStarted` and `errorMessage` for lifecycle and failure handling.

## Common Rendering Pattern

```kotlin
when (state.uiState) {
    QiblaUiState.PERMISSION_REQUIRED -> PermissionContent(
        onAllow = controller::requestPermission,
    )
    QiblaUiState.LOCATION_DISABLED -> LocationSettingsContent(
        onOpenSettings = controller::openLocationSettings,
        onRetry = controller::retryLocation,
    )
    QiblaUiState.ALIGNED -> AlignedContent(state.compass)
    else -> CompassContent(state.compass)
}
```

## Reusable Default Pieces

You can mix custom layout with default components:

```kotlin
QiblaCompassDial(state = state)
QiblaStateMessage(state = state)
QiblaStatusPanel(state = state)
```

These components accept `QiblaStyle` and `QiblaStrings` if you need custom colors or localized copy.
