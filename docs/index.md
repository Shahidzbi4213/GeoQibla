# GeoQibla Documentation

<span class="geoqibla-version">Version 0.0.1</span>

**Kotlin Multiplatform Compose Qibla direction UI** for Android and iOS apps. Use the default screen when you want a complete compass flow, or subscribe to controller state when you need a custom interface.

## Get Started in 60 Seconds

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.shahidzbi4213:geoqibla:0.0.1")
        }
    }
}
```

```kotlin
import androidx.compose.runtime.Composable
import com.shahid.tech.qibla.GeoQiblaScreen
import com.shahid.tech.qibla.rememberQiblaController

@Composable
fun QiblaRoute() {
    val controller = rememberQiblaController()
    GeoQiblaScreen(controller = controller)
}
```

`GeoQiblaScreen` starts the controller while it is in composition and stops it when it leaves composition.

## Features

<div class="geoqibla-grid" markdown>

- **Compose-first default UI** with compact and wide layouts.
- **Headless controller** through `QiblaController.state`.
- **Location and heading state** for permissions, settings, calibration, tilt, and errors.
- **Qibla bearing and distance** calculated from the current fix.
- **Configurable alignment** thresholds, smoothing, haptics, and callbacks.
- **Styling, localization, and slots** for replacing visible regions.

</div>

## Installation

Start with these pages:

- [Installation](getting-started/installation.md) - dependency setup and platform requirements.
- [Quick Start](getting-started/quick-start.md) - first working default screen.
- [Configuration](getting-started/configuration.md) - tune runtime behavior.
- [Android example](examples/android.md) and [iOS example](examples/ios.md) - platform copy-paste setup.

## Core Concepts

### Default Screen

`GeoQiblaScreen` renders a complete compass experience from a `QiblaController`. It shows state messages, action buttons, a compass dial, a target badge, and status rows.

### Controller State

`rememberQiblaController()` creates a controller backed by platform location and heading services. Its `state` is a `StateFlow<QiblaState>` containing the current UI state, compass data, location data, sensor accuracy, orientation source, and optional error message.

### Customization

Use `QiblaStyle` for visual tokens, `QiblaStrings` for copy and layout direction, and `QiblaSlots` for replacing default regions while keeping the controller behavior.

## Platform Requirements

| Platform | Minimum | Runtime services |
| --- | --- | --- |
| Android | API 26 | `LocationManager`, `SensorManager`, foreground location permission |
| iOS | iOS target supported by your KMP app | `CoreLocation`, heading updates, when-in-use location permission |

## Support

- [GitHub repository](https://github.com/Shahidzbi4213/GeoQibla)
- [Troubleshooting](troubleshooting.md)
- [Contributing](contributing.md)

## License

Apache 2.0. See [License](license.md).
