# Installation

GeoQibla is published as a Kotlin Multiplatform artifact:

```kotlin
implementation("io.github.shahidzbi4213:geoqibla:0.0.1")
```

## KMP Dependency Setup

Add the dependency to the `commonMain` source set of the module that renders your shared Compose UI.

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.shahidzbi4213:geoqibla:0.0.1")
        }
    }
}
```

## Version Catalog Setup

If your project uses `libs.versions.toml`, define the version and library once:

```toml
[versions]
geoqibla = "0.0.1"

[libraries]
geoqibla = { module = "io.github.shahidzbi4213:geoqibla", version.ref = "geoqibla" }
```

Then depend on it from the KMP module:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.geoqibla)
        }
    }
}
```

## Android Notes

GeoQibla targets Android API 26 or newer. The library manifest declares foreground location permissions:

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

The default UI asks for permission at runtime with the Activity Result APIs. If your app has custom manifest merging rules, confirm those permissions appear in the merged app manifest.

## iOS Notes

iOS apps must provide location usage text in `Info.plist`:

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>GeoQibla uses your location to calculate the direction of the Qibla.</string>
```

If your app already has a broader location permission string, use copy that matches your product's privacy language.

## Sync and Verify

After adding the dependency:

1. Sync Gradle.
2. Build the shared module.
3. Run an Android or iOS target on a physical device.
4. Confirm the default screen can request location permission and then show heading data.

Simulators and emulators can validate layout and permission states, but compass behavior should be verified on real hardware.
