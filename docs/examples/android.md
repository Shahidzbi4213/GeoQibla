---
title: Android Integration & Example - GeoQibla
description: Copy-paste Android setup for GeoQibla in Kotlin Multiplatform. Gradle dependency, AndroidManifest permissions, and Compose screen implementation.
keywords:
  - android qibla example
  - android qibla compass kmp
  - android location permissions qibla
  - geoqibla android integration
---

# Android Example

## Gradle

Add GeoQibla to your KMP shared module:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.shahidzbi4213:geoqibla:0.0.4")
        }
    }
}
```

## Manifest

The library declares foreground location permissions in its Android manifest. Confirm the merged application manifest includes:

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## Activity

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.shahid.tech.qibla.GeoQiblaScreen
import com.shahid.tech.qibla.rememberQiblaController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    val controller = rememberQiblaController()
    GeoQiblaScreen(controller = controller)
}
```

## Device Testing

Use a physical Android device for compass validation. Emulators can validate permission and layout flows, but heading sensors and magnetic field behavior are device-dependent.
