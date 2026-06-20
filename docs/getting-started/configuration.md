# Configuration

`QiblaConfig` controls the runtime thresholds used by the default controller.

```kotlin
val controller = rememberQiblaController(
    config = QiblaConfig(
        nearDegrees = 8f,
        alignedDegrees = 2f,
        stableAlignedDurationMillis = 900L,
        onAligned = { /* record alignment */ },
    ),
)
```

## Options

| Property                       |  Default | Description                                                                             |
|--------------------------------|---------:|-----------------------------------------------------------------------------------------|
| `nearDegrees`                  |    `10f` | Absolute heading delta that marks the user near Qibla.                                  |
| `alignedDegrees`               |     `3f` | Absolute heading delta required before stable alignment can start.                      |
| `stableAlignedDurationMillis`  |   `750L` | Time the heading must stay within `alignedDegrees` before `ALIGNED`.                    |
| `smoothingFactor`              |  `0.15f` | Heading smoothing factor from `0f` to `1f`; larger values follow sensor changes faster. |
| `locationUpdateIntervalMillis` | `1_000L` | Android location update interval passed to platform location updates.                   |
| `tiltLimitDegrees`             |    `55f` | Maximum device pitch or roll before the UI enters `TILTED`.                             |
| `magneticFieldMinMicrotesla`   |    `25f` | Lower magnetic field warning bound.                                                     |
| `magneticFieldMaxMicrotesla`   |    `65f` | Upper magnetic field warning bound.                                                     |
| `hapticsEnabled`               |   `true` | Performs a platform haptic when stable alignment is first reached.                      |
| `onAligned`                    |   `null` | Callback invoked once per stable alignment event.                                       |

## Validation Rules

`QiblaConfig` validates values when it is created:

- `nearDegrees` must be greater than or equal to `alignedDegrees`.
- `alignedDegrees` must be non-negative.
- `stableAlignedDurationMillis` must be non-negative.
- `smoothingFactor` must be between `0f` and `1f`.
- `locationUpdateIntervalMillis` must be positive.
- `tiltLimitDegrees` must be between `0f` and `90f`.
- `magneticFieldMinMicrotesla` must be less than or equal to `magneticFieldMaxMicrotesla`.

## Alignment Behavior

The controller tracks the absolute angular distance between the current heading and the Qibla bearing:

- `NEAR_QIBLA` is used when the delta is within `nearDegrees`.
- `ALIGNED` is used only after the delta stays within `alignedDegrees` for `stableAlignedDurationMillis`.
- `onAligned` and haptics are delivered once for each stable alignment run.
- Moving outside the aligned threshold resets the stable alignment timer.

## Sensor Warnings

The UI may enter `CALIBRATION_NEEDED` when sensor accuracy is unreliable or low, or when magnetic field strength falls outside the configured microtesla bounds. The user can dismiss calibration from the default UI.

The UI may enter `TILTED` when the device pitch or roll exceeds `tiltLimitDegrees`.
