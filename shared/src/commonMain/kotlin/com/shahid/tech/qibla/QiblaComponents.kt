package com.shahid.tech.qibla

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun QiblaCompassDial(
    state: QiblaState,
    modifier: Modifier = Modifier,
    style: QiblaStyle = QiblaStyle.default(),
    strings: QiblaStrings = QiblaStrings.default(),
) {
    val direction = state.compass.directionToQiblaDegrees ?: 0f
    val animatedDirection by animateFloatAsState(
        targetValue = direction,
        animationSpec = tween(style.animation.compassRotationMillis),
        label = "qibla-direction",
    )
    val targetColor = when {
        state.compass.isAligned -> style.colors.aligned
        state.compass.isNearQibla -> style.colors.near
        else -> style.colors.qibla
    }
    val alignmentProgress by animateFloatAsState(
        targetValue = if (state.compass.isAligned) 1f else 0f,
        animationSpec = tween(style.animation.stateChangeMillis * 2),
        label = "qibla-alignment",
    )

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = strings.compassContentDescription },
        shape = CircleShape,
        color = style.colors.surface,
        contentColor = style.colors.content,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(2.dp, style.colors.warning.copy(alpha = 0.55f)),
    ) {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f - 4.dp.toPx()

                drawCircle(
                    color = style.colors.surface,
                    radius = radius,
                    center = center,
                )
                drawCircle(
                    color = style.colors.warning.copy(alpha = 0.55f),
                    radius = radius - 3.dp.toPx(),
                    center = center,
                    style = Stroke(width = 5.dp.toPx()),
                )
                drawCircle(
                    color = style.colors.outline,
                    radius = radius - 11.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx()),
                )

                repeat(72) { index ->
                    val isMajor = index % 6 == 0
                    rotate(degrees = index * 5f, pivot = center) {
                        drawLine(
                            color = if (isMajor) {
                                style.colors.warning.copy(alpha = 0.72f)
                            } else {
                                style.colors.secondaryContent.copy(alpha = 0.32f)
                            },
                            start = Offset(
                                center.x,
                                center.y - radius + if (isMajor) 16.dp.toPx() else 19.dp.toPx(),
                            ),
                            end = Offset(
                                center.x,
                                center.y - radius + if (isMajor) 31.dp.toPx() else 27.dp.toPx(),
                            ),
                            strokeWidth = if (isMajor) 1.5.dp.toPx() else 0.75.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }

                drawCircle(
                    color = style.colors.outline.copy(alpha = 0.65f),
                    radius = radius * 0.62f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx()),
                )

                repeat(16) { index ->
                    rotate(degrees = index * 22.5f, pivot = center) {
                        val isLongPoint = index % 2 == 0
                        val rosePoint = Path().apply {
                            moveTo(
                                center.x,
                                center.y - radius * if (isLongPoint) 0.52f else 0.38f,
                            )
                            lineTo(
                                center.x - if (isLongPoint) 7.dp.toPx() else 4.dp.toPx(),
                                center.y,
                            )
                            lineTo(
                                center.x + if (isLongPoint) 7.dp.toPx() else 4.dp.toPx(),
                                center.y,
                            )
                            close()
                        }
                        drawPath(
                            path = rosePoint,
                            color = style.colors.content.copy(
                                alpha = if (isLongPoint) 0.25f else 0.10f,
                            ),
                        )
                    }
                }

                rotate(degrees = animatedDirection, pivot = center) {
                    val markerCenter = Offset(center.x, center.y - radius + 28.dp.toPx())
                    val southNeedle = Path().apply {
                        moveTo(center.x, center.y + radius * 0.47f)
                        lineTo(center.x - 7.dp.toPx(), center.y - 2.dp.toPx())
                        lineTo(center.x + 7.dp.toPx(), center.y - 2.dp.toPx())
                        close()
                    }
                    drawPath(
                        path = southNeedle,
                        color = style.colors.error.copy(alpha = 0.82f),
                    )
                    val qiblaNeedle = Path().apply {
                        moveTo(center.x, markerCenter.y + 18.dp.toPx())
                        lineTo(center.x - 7.dp.toPx(), center.y + 2.dp.toPx())
                        lineTo(center.x + 7.dp.toPx(), center.y + 2.dp.toPx())
                        close()
                    }
                    drawPath(path = qiblaNeedle, color = targetColor)
                    drawCircle(
                        color = targetColor.copy(alpha = 0.18f + alignmentProgress * 0.12f),
                        radius = (24f + alignmentProgress * 4f).dp.toPx(),
                        center = markerCenter,
                    )
                    drawCircle(
                        color = style.colors.surface,
                        radius = 20.dp.toPx(),
                        center = markerCenter,
                    )
                    drawCircle(
                        color = targetColor,
                        radius = 20.dp.toPx(),
                        center = markerCenter,
                        style = Stroke(width = 1.dp.toPx()),
                    )
                    drawCircle(
                        color = style.colors.content,
                        radius = 7.dp.toPx(),
                        center = center,
                    )
                    drawCircle(
                        color = style.colors.surface,
                        radius = 3.dp.toPx(),
                        center = center,
                    )

                    rotate(degrees = -animatedDirection, pivot = markerCenter) {
                        val iconWidth = 24.dp.toPx()
                        val iconHeight = 20.dp.toPx()
                        val iconTopLeft = Offset(
                            markerCenter.x - iconWidth / 2f,
                            markerCenter.y - iconHeight / 2f,
                        )
                        drawRoundRect(
                            color = style.colors.content,
                            topLeft = iconTopLeft,
                            size = Size(iconWidth, iconHeight),
                            cornerRadius = CornerRadius(2.dp.toPx()),
                        )
                        drawRect(
                            color = targetColor,
                            topLeft = Offset(iconTopLeft.x, iconTopLeft.y + 7.dp.toPx()),
                            size = Size(iconWidth, 4.dp.toPx()),
                        )
                        drawRect(
                            color = targetColor.copy(alpha = 0.72f),
                            topLeft = Offset(
                                markerCenter.x - 3.dp.toPx(),
                                iconTopLeft.y + 15.dp.toPx(),
                            ),
                            size = Size(6.dp.toPx(), 9.dp.toPx()),
                        )
                    }
                }
            }

            CompassCardinalLabels(style)
        }
    }
}

@Composable
private fun BoxScope.CompassCardinalLabels(style: QiblaStyle) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Text(
            text = "N",
            style = style.typography.label,
            color = style.colors.content,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 62.dp),
        )
        Text(
            text = "E",
            style = style.typography.label,
            color = style.colors.content,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 54.dp),
        )
        Text(
            text = "S",
            style = style.typography.label,
            color = style.colors.content,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
        )
        Text(
            text = "W",
            style = style.typography.label,
            color = style.colors.content,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 54.dp),
        )
    }
}

@Composable
fun QiblaStatusPanel(
    state: QiblaState,
    modifier: Modifier = Modifier,
    style: QiblaStyle = QiblaStyle.default(),
    strings: QiblaStrings = QiblaStrings.default(),
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = style.shapes.panel,
        color = style.colors.surface,
        contentColor = style.colors.content,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, style.colors.outline.copy(alpha = 0.75f)),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(style.dimensions.panelPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatusMetric(
                    label = strings.currentHeadingLabel,
                    value = state.compass.azimuthDegrees.formatDegrees(strings),
                    style = style,
                    modifier = Modifier.weight(1f),
                )
                MetricDivider(style)
                StatusMetric(
                    label = strings.distanceLabel,
                    value = state.compass.distanceToKaabaMeters.formatDistance(strings),
                    style = style,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            StatusRow(
                label = strings.directionDeltaLabel,
                value = state.compass.directionToQiblaDegrees.formatDirection(strings),
                style = style,
            )
            StatusRow(
                label = strings.locationLabel,
                value = state.location.label ?: strings.unavailableValue,
                style = style,
            )
            StatusRow(
                label = strings.accuracyLabel,
                value = state.sensorAccuracy.displayName(),
                style = style,
            )
            StatusRow(
                label = strings.sourceLabel,
                value = state.orientationSource.displayName(),
                style = style,
            )
        }
    }
}

@Composable
fun QiblaStateMessage(
    state: QiblaState,
    modifier: Modifier = Modifier,
    style: QiblaStyle = QiblaStyle.default(),
    strings: QiblaStrings = QiblaStrings.default(),
) {
    val message = strings.messageFor(state)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = message.title,
            style = style.typography.statusTitle,
            color = stateAccentColor(state, style),
        )
        Text(
            text = message.body,
            style = style.typography.body,
            color = style.colors.secondaryContent,
        )
    }
}

@Composable
internal fun QiblaTargetBadge(
    state: QiblaState,
    style: QiblaStyle,
    strings: QiblaStrings,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = stateAccentColor(state, style),
        ) {}
        Text(
            text = strings.messageFor(state).title,
            style = style.typography.label,
            color = style.colors.secondaryContent,
            maxLines = 1,
        )
    }
}

@Composable
private fun RowScope.StatusMetric(
    label: String,
    value: String,
    style: QiblaStyle,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = style.typography.statusTitle,
            color = style.colors.content,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = label,
            style = style.typography.label,
            color = style.colors.secondaryContent,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun RowScope.MetricDivider(style: QiblaStyle) {
    Spacer(
        Modifier
            .width(1.dp)
            .height(48.dp)
            .background(style.colors.outline),
    )
}

@Composable
internal fun QiblaActionButtons(
    state: QiblaState,
    controller: QiblaController,
    style: QiblaStyle,
    strings: QiblaStrings,
    slots: QiblaSlots,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val customActions = slots.actionButtons
        if (customActions != null) {
            customActions(state)
        } else {
            DefaultActionButtons(state, controller, style, strings)
        }
    }
}

@Composable
private fun RowScope.DefaultActionButtons(
    state: QiblaState,
    controller: QiblaController,
    style: QiblaStyle,
    strings: QiblaStrings,
) {
    when (state.uiState) {
        QiblaUiState.IDLE -> PrimaryAction(
            text = strings.retryAction,
            style = style,
            onClick = controller::start,
        )

        QiblaUiState.REQUESTING_PERMISSION,
        QiblaUiState.PERMISSION_REQUIRED,
        QiblaUiState.PERMISSION_DENIED,
        -> PrimaryAction(
            text = strings.requestPermissionAction,
            style = style,
            onClick = controller::requestPermission,
        )

        QiblaUiState.PERMISSION_PERMANENTLY_DENIED -> PrimaryAction(
            text = strings.openSettingsAction,
            style = style,
            onClick = controller::openAppSettings,
        )

        QiblaUiState.LOCATION_DISABLED -> {
            PrimaryAction(
                text = strings.openLocationSettingsAction,
                style = style,
                onClick = controller::openLocationSettings,
            )
            SecondaryAction(
                text = strings.retryAction,
                style = style,
                onClick = controller::retryLocation,
            )
        }

        QiblaUiState.SENSOR_UNAVAILABLE,
        QiblaUiState.ERROR,
        -> PrimaryAction(
            text = strings.retryAction,
            style = style,
            onClick = controller::retryLocation,
        )

        QiblaUiState.CALIBRATION_NEEDED -> SecondaryAction(
            text = strings.dismissAction,
            style = style,
            onClick = controller::dismissCalibration,
        )

        else -> Spacer(Modifier.height(0.dp))
    }
}

@Composable
private fun RowScope.PrimaryAction(
    text: String,
    style: QiblaStyle,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.weight(1f),
        onClick = onClick,
        shape = style.shapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.colors.primary,
            contentColor = style.colors.onPrimary,
        ),
    ) {
        Text(text = text, style = style.typography.body)
    }
}

@Composable
private fun RowScope.SecondaryAction(
    text: String,
    style: QiblaStyle,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = Modifier.weight(1f),
        onClick = onClick,
        shape = style.shapes.button,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = style.colors.primary,
        ),
    ) {
        Text(text = text, style = style.typography.body)
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    style: QiblaStyle,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = style.dimensions.statusRowMinHeight),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = style.typography.label,
            color = style.colors.secondaryContent,
            modifier = Modifier.weight(0.44f),
        )
        Text(
            text = value,
            style = style.typography.body,
            color = style.colors.content,
            modifier = Modifier.weight(0.56f),
            textAlign = TextAlign.End,
        )
    }
}

private fun stateAccentColor(state: QiblaState, style: QiblaStyle): Color =
    when (state.uiState) {
        QiblaUiState.ALIGNED -> style.colors.aligned
        QiblaUiState.NEAR_QIBLA -> style.colors.near
        QiblaUiState.ERROR,
        QiblaUiState.PERMISSION_DENIED,
        QiblaUiState.PERMISSION_PERMANENTLY_DENIED,
        -> style.colors.error

        QiblaUiState.CALIBRATION_NEEDED,
        QiblaUiState.TILTED,
        QiblaUiState.LOCATION_DISABLED,
        -> style.colors.warning

        else -> style.colors.primary
    }

private fun Float?.formatDegrees(strings: QiblaStrings): String =
    this?.let { "${it.roundToInt()}°" } ?: strings.unavailableValue

private fun Float?.formatDirection(strings: QiblaStrings): String {
    val value = this ?: return strings.unavailableValue
    if (abs(value) < 0.5f) return strings.straightValue
    val side = if (value > 0f) strings.rightValue else strings.leftValue
    return "${abs(value).roundToInt()}° $side"
}

private fun Double?.formatDistance(strings: QiblaStrings): String {
    val meters = this ?: return strings.unavailableValue
    return if (meters >= 100_000.0) {
        "${(meters / 1_000.0).roundToInt()} km"
    } else {
        "${meters.roundToInt()} m"
    }
}

private fun QiblaSensorAccuracy.displayName(): String =
    name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

private fun QiblaOrientationSource.displayName(): String =
    name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
