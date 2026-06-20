# Quick Start

This page builds the default GeoQibla flow with the smallest useful setup.

## Default Screen

```kotlin
import androidx.compose.runtime.Composable
import com.shahid.tech.qibla.GeoQiblaScreen
import com.shahid.tech.qibla.rememberQiblaController

@Composable
fun QiblaRoute() {
    val controller = rememberQiblaController()

    GeoQiblaScreen(
        controller = controller,
    )
}
```

`GeoQiblaScreen` handles lifecycle start and stop for the controller while the composable is visible.

## First Working Example

Use it directly inside your app's root screen:

```kotlin
import androidx.compose.runtime.Composable
import com.shahid.tech.qibla.GeoQiblaScreen
import com.shahid.tech.qibla.QiblaConfig
import com.shahid.tech.qibla.rememberQiblaController

@Composable
fun App() {
    val controller = rememberQiblaController(
        config = QiblaConfig(
            onAligned = {
                // Called after stable alignment with Qibla.
            },
        ),
    )

    GeoQiblaScreen(controller = controller)
}
```

## Lifecycle Behavior

`rememberQiblaController()` creates one controller for the current composition. It is configured from `QiblaConfig`, observes platform services, and exposes a `StateFlow<QiblaState>`.

The default screen:

- Calls `controller.start()` when it enters composition.
- Calls `controller.stop()` when it leaves composition.
- Shows permission, location, calibration, tilt, ready, near, aligned, and error states.
- Wires default action buttons to the controller methods.

## Next Steps

- Use [configuration](configuration.md) to tune thresholds and callbacks.
- Use [custom UI](../guides/custom-ui.md) when the built-in screen is too opinionated.
- Use [styling and localization](../guides/styling-localization.md) to match your product.
