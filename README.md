# GeoQibla

GeoQibla is a Kotlin Multiplatform Compose library for showing Qibla direction
in Android and iOS apps. It provides a ready-made shared screen and a headless
controller for custom UI.

## Install

Add the dependency in your KMP module's `commonMain` source set:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.shahid-iqbal:geoqibla:0.0.1")
        }
    }
}
```

## Usage

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

## Custom UI

Use `QiblaController.state` when you want to build your own screen:

```kotlin
val controller = rememberQiblaController()
val state by controller.state.collectAsState()
```

The default UI can also be customized with:

- `QiblaStyle`
- `QiblaStrings`
- `QiblaSlots`

## Platform Notes

Android apps should use minSdk 26 or newer. GeoQibla requests foreground
location access through the shared UI and uses Android framework location and
sensor APIs.

iOS apps should include location usage text in `Info.plist`:

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>GeoQibla uses your location to calculate the direction of the Qibla.</string>
```
