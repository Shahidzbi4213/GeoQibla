# Slots and Components

Use `QiblaSlots` when you want to replace one region of the default screen without rebuilding the whole flow.

```kotlin
GeoQiblaScreen(
    controller = controller,
    slots = QiblaSlots(
        topBar = { state ->
            MyQiblaHeader(isAligned = state.compass.isAligned)
        },
        actionButtons = { state ->
            MyActions(state)
        },
    ),
)
```

## Slot Points

| Slot | Replaces |
| --- | --- |
| `topBar` | Title, subtitle, and target badge region. |
| `compassDial` | Compass dial region. |
| `targetBadge` | Badge inside the default top bar. |
| `statusRows` | Status panel region. |
| `stateMessage` | Message panel region. |
| `calibrationSheet` | Calibration prompt when `CALIBRATION_NEEDED`. |
| `actionButtons` | Default action row. |

## Public Components

GeoQibla exposes these default components for reuse:

```kotlin
QiblaCompassDial(state = state)
QiblaStatusPanel(state = state)
QiblaStateMessage(state = state)
```

They work with the same `QiblaState`, `QiblaStyle`, and `QiblaStrings` models as the default screen.

## Replacement Strategy

Prefer slots when the default screen structure is mostly right. Prefer a full custom screen when:

- You need a completely different navigation or layout model.
- You want to control controller lifecycle manually.
- You need custom permission or settings flows.
