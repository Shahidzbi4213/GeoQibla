# QiblaConfig

```kotlin
data class QiblaConfig(
    val nearDegrees: Float = 10f,
    val alignedDegrees: Float = 3f,
    val stableAlignedDurationMillis: Long = 750L,
    val smoothingFactor: Float = 0.15f,
    val locationUpdateIntervalMillis: Long = 1_000L,
    val tiltLimitDegrees: Float = 55f,
    val magneticFieldMinMicrotesla: Float = 25f,
    val magneticFieldMaxMicrotesla: Float = 65f,
    val hapticsEnabled: Boolean = true,
    val onAligned: (() -> Unit)? = null,
)
```

See [Configuration](../getting-started/configuration.md) for behavior and validation details.

## Usage

```kotlin
val controller = rememberQiblaController(
    config = QiblaConfig(
        nearDegrees = 8f,
        alignedDegrees = 2f,
        hapticsEnabled = false,
    ),
)
```
