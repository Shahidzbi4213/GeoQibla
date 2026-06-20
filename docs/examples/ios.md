# iOS Example

## Info.plist

Add when-in-use location usage text:

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>GeoQibla uses your location to calculate the direction of the Qibla.</string>
```

## Compose Entry Point

From your shared iOS entry point:

```kotlin
import androidx.compose.ui.window.ComposeUIViewController
import com.shahid.tech.qibla.GeoQiblaScreen
import com.shahid.tech.qibla.rememberQiblaController

fun MainViewController() = ComposeUIViewController {
    val controller = rememberQiblaController()
    GeoQiblaScreen(controller = controller)
}
```

## SwiftUI Host

Host the generated Compose view controller from SwiftUI in the same way as other Compose Multiplatform iOS screens.

## Device Testing

Use a physical iPhone for heading validation. iOS simulator behavior is useful for layout work but not for final compass accuracy checks.
