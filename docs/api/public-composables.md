# Public Composables

GeoQibla exposes reusable pieces from the default UI.

## QiblaCompassDial

```kotlin
@Composable
fun QiblaCompassDial(
    state: QiblaState,
    modifier: Modifier = Modifier,
    style: QiblaStyle = QiblaStyle.default(),
    strings: QiblaStrings = QiblaStrings.default(),
)
```

Draws the compass dial, Qibla bearing label, and animated direction marker.

## QiblaStatusPanel

```kotlin
@Composable
fun QiblaStatusPanel(
    state: QiblaState,
    modifier: Modifier = Modifier,
    style: QiblaStyle = QiblaStyle.default(),
    strings: QiblaStrings = QiblaStrings.default(),
)
```

Shows bearing, current heading, adjustment, distance, location label, sensor accuracy, and orientation source.

## QiblaStateMessage

```kotlin
@Composable
fun QiblaStateMessage(
    state: QiblaState,
    modifier: Modifier = Modifier,
    style: QiblaStyle = QiblaStyle.default(),
    strings: QiblaStrings = QiblaStrings.default(),
)
```

Shows a localized title and body for the current `QiblaUiState`.
