# GeoQibla

[Documentation](https://shahidzbi4213.github.io/GeoQibla/) | [GitHub](https://github.com/Shahidzbi4213/GeoQibla)

GeoQibla is a Kotlin Multiplatform Compose library for showing Qibla direction in Android and iOS apps. It provides a ready-made shared screen, a headless controller for custom UI, and styling, string, and slot APIs for product-specific presentation.

Current version: `0.0.1`

## Install

Add the dependency in your KMP module's `commonMain` source set:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.shahidzbi4213:geoqibla:0.0.1")
        }
    }
}
```

## Quick Usage

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

## API Overview

- `GeoQiblaScreen`: ready-made adaptive screen with compass, status, messages, and action buttons.
- `rememberQiblaController`: creates a controller backed by Android or iOS platform services.
- `QiblaController.state`: `StateFlow<QiblaState>` for custom UIs.
- `QiblaConfig`: alignment thresholds, smoothing, location interval, tilt and magnetic limits, haptics, and `onAligned`.
- `QiblaStyle`, `QiblaStrings`, and `QiblaSlots`: styling, localization, RTL behavior, and region replacement.
- `QiblaCompassDial`, `QiblaStatusPanel`, and `QiblaStateMessage`: reusable public UI components.

## Custom UI

```kotlin
val controller = rememberQiblaController()
val state by controller.state.collectAsState()
```

Use `state.uiState`, `state.compass`, and `state.location` to render your own flow while delegating platform location and heading behavior to GeoQibla.

## Platform Notes

Android apps should use minSdk 26 or newer. GeoQibla declares foreground location permissions in its Android library manifest, requests runtime location permission from the default UI, and uses Android framework location and sensor APIs.

iOS apps should include location usage text in `Info.plist`:

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>GeoQibla uses your location to calculate the direction of the Qibla.</string>
```

Compass behavior should be verified on physical devices. Emulators and simulators are useful for layout and permission-state checks, but not final heading accuracy.

## Documentation

The full docs site is configured with MkDocs Material and published to GitHub Pages:

- [Getting started](https://shahidzbi4213.github.io/GeoQibla/getting-started/installation/)
- [Configuration](https://shahidzbi4213.github.io/GeoQibla/getting-started/configuration/)
- [Custom UI](https://shahidzbi4213.github.io/GeoQibla/guides/custom-ui/)
- [Troubleshooting](https://shahidzbi4213.github.io/GeoQibla/troubleshooting/)

## License

Apache 2.0.
