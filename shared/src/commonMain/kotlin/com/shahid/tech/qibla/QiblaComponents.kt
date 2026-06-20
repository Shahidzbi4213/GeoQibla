package com.shahid.tech.qibla

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = strings.compassContentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f
            val stroke = 2.dp.toPx()

            drawCircle(
                color = style.colors.surface,
                radius = radius,
                center = center,
            )
            drawCircle(
                color = style.colors.outline,
                radius = radius - stroke,
                center = center,
                style = Stroke(width = stroke),
            )

            repeat(36) { index ->
                val isMajor = index % 3 == 0
                rotate(degrees = index * 10f, pivot = center) {
                    drawLine(
                        color = style.colors.compassTick,
                        start = Offset(center.x, center.y - radius + if (isMajor) 14.dp.toPx() else 18.dp.toPx()),
                        end = Offset(center.x, center.y - radius + 32.dp.toPx()),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }

            rotate(degrees = animatedDirection, pivot = center) {
                val arrow = Path().apply {
                    moveTo(center.x, center.y - radius + 44.dp.toPx())
                    lineTo(center.x - 14.dp.toPx(), center.y + 12.dp.toPx())
                    lineTo(center.x, center.y - 2.dp.toPx())
                    lineTo(center.x + 14.dp.toPx(), center.y + 12.dp.toPx())
                    close()
                }
                drawPath(arrow, color = targetColor)
                drawLine(
                    color = targetColor,
                    start = center,
                    end = Offset(center.x, center.y - radius + 58.dp.toPx()),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(CircleShape)
                .background(style.colors.surfaceVariant)
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Text(
                text = state.compass.qiblaBearingDegrees.formatDegrees(strings),
                style = style.typography.numeric,
                color = style.colors.content,
                textAlign = TextAlign.Center,
            )
            Text(
                text = strings.qiblaBearingLabel,
                style = style.typography.label,
                color = style.colors.secondaryContent,
                textAlign = TextAlign.Center,
            )
        }
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
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(style.dimensions.panelPadding),
        ) {
            StatusRow(
                label = strings.qiblaBearingLabel,
                value = state.compass.qiblaBearingDegrees.formatDegrees(strings),
                style = style,
            )
            StatusRow(
                label = strings.currentHeadingLabel,
                value = state.compass.azimuthDegrees.formatDegrees(strings),
                style = style,
            )
            StatusRow(
                label = strings.directionDeltaLabel,
                value = state.compass.directionToQiblaDegrees.formatDirection(strings),
                style = style,
            )
            StatusRow(
                label = strings.distanceLabel,
                value = state.compass.distanceToKaabaMeters.formatDistance(strings),
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = style.shapes.panel,
        color = when (state.uiState) {
            QiblaUiState.ALIGNED -> style.colors.aligned.copy(alpha = 0.12f)
            QiblaUiState.NEAR_QIBLA -> style.colors.near.copy(alpha = 0.14f)
            QiblaUiState.ERROR,
            QiblaUiState.PERMISSION_DENIED,
            QiblaUiState.PERMISSION_PERMANENTLY_DENIED,
            -> style.colors.error.copy(alpha = 0.10f)

            QiblaUiState.CALIBRATION_NEEDED,
            QiblaUiState.TILTED,
            QiblaUiState.LOCATION_DISABLED,
            -> style.colors.warning.copy(alpha = 0.12f)

            else -> style.colors.surface
        },
        contentColor = style.colors.content,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(style.dimensions.panelPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
}

@Composable
internal fun QiblaTargetBadge(
    state: QiblaState,
    style: QiblaStyle,
    strings: QiblaStrings,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = style.shapes.badge,
        color = stateAccentColor(state, style).copy(alpha = 0.12f),
        contentColor = stateAccentColor(state, style),
    ) {
        Text(
            text = strings.messageFor(state).title,
            style = style.typography.label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
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
