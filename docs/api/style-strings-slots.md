# Style, Strings, and Slots

## QiblaStyle

```kotlin
data class QiblaStyle(
    val colors: QiblaColors = QiblaColors(),
    val dimensions: QiblaDimensions = QiblaDimensions(),
    val typography: QiblaTypography = QiblaTypography(),
    val shapes: QiblaShapes = QiblaShapes(),
    val animation: QiblaAnimationTimings = QiblaAnimationTimings(),
)
```

`QiblaStyle.default()` returns the built-in style.

## QiblaStrings

`QiblaStrings` contains every visible default UI string plus `layoutDirection`.

Factory methods:

- `QiblaStrings.default()`
- `QiblaStrings.english()`
- `QiblaStrings.arabic()`

## QiblaSlots

```kotlin
class QiblaSlots(
    val topBar: (@Composable (QiblaState) -> Unit)? = null,
    val compassDial: (@Composable (QiblaState) -> Unit)? = null,
    val targetBadge: (@Composable (QiblaState) -> Unit)? = null,
    val statusRows: (@Composable (QiblaState) -> Unit)? = null,
    val stateMessage: (@Composable (QiblaState) -> Unit)? = null,
    val calibrationSheet: (@Composable (QiblaState, onDismiss: () -> Unit) -> Unit)? = null,
    val actionButtons: (@Composable RowScope.(QiblaState) -> Unit)? = null,
)
```

`QiblaSlots.default()` returns a slot set that lets the default screen render all built-in regions.
