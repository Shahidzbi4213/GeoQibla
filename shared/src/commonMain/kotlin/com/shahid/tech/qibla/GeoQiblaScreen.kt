package com.shahid.tech.qibla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun GeoQiblaScreen(
    controller: QiblaController,
    modifier: Modifier = Modifier,
    style: QiblaStyle = QiblaStyle.default(),
    strings: QiblaStrings = QiblaStrings.default(),
    slots: QiblaSlots = QiblaSlots.default(),
) {
    val state by controller.state.collectAsState()

    DisposableEffect(controller) {
        controller.start()
        onDispose { controller.stop() }
    }

    CompositionLocalProvider(LocalLayoutDirection provides strings.layoutDirection) {
        MaterialTheme {
            Surface(
                modifier = modifier.fillMaxSize(),
                color = style.colors.background,
                contentColor = style.colors.content,
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeContentPadding(),
                ) {
                    val screenPadding = if (maxWidth < 420.dp) {
                        style.dimensions.compactScreenPadding
                    } else {
                        style.dimensions.screenPadding
                    }
                    val isWide = maxWidth >= 720.dp
                    val compassSize = maxWidth
                        .coerceAtMost(style.dimensions.compassMaxSize)
                        .coerceAtLeast(style.dimensions.compassMinSize)

                    if (isWide) {
                        WideGeoQiblaContent(
                            state = state,
                            controller = controller,
                            style = style,
                            strings = strings,
                            slots = slots,
                            compassSize = style.dimensions.compassMaxSize,
                            modifier = Modifier.padding(screenPadding),
                        )
                    } else {
                        CompactGeoQiblaContent(
                            state = state,
                            controller = controller,
                            style = style,
                            strings = strings,
                            slots = slots,
                            compassSize = compassSize,
                            modifier = Modifier.padding(screenPadding),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactGeoQiblaContent(
    state: QiblaState,
    controller: QiblaController,
    style: QiblaStyle,
    strings: QiblaStrings,
    slots: QiblaSlots,
    compassSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(style.dimensions.contentGap),
    ) {
        TopBar(state, style, strings, slots)
        DialRegion(state, style, strings, slots, Modifier.size(compassSize))
        MessageRegion(state, controller, style, strings, slots)
        StatusRowsRegion(state, style, strings, slots)
    }
}

@Composable
private fun WideGeoQiblaContent(
    state: QiblaState,
    controller: QiblaController,
    style: QiblaStyle,
    strings: QiblaStrings,
    slots: QiblaSlots,
    compassSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier,
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(style.dimensions.contentGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(0.48f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(style.dimensions.contentGap),
        ) {
            DialRegion(state, style, strings, slots, Modifier.size(compassSize))
        }
        Column(
            modifier = Modifier
                .weight(0.52f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(style.dimensions.contentGap),
        ) {
            TopBar(state, style, strings, slots)
            MessageRegion(state, controller, style, strings, slots)
            StatusRowsRegion(state, style, strings, slots)
        }
    }
}

@Composable
private fun TopBar(
    state: QiblaState,
    style: QiblaStyle,
    strings: QiblaStrings,
    slots: QiblaSlots,
) {
    val customTopBar = slots.topBar
    if (customTopBar != null) {
        customTopBar(state)
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.title,
                style = style.typography.title,
                color = style.colors.content,
            )
            val targetBadge = slots.targetBadge
            if (targetBadge != null) {
                targetBadge(state)
            } else {
                QiblaTargetBadge(state, style, strings)
            }
        }
        Text(
            text = strings.subtitle,
            style = style.typography.subtitle,
            color = style.colors.secondaryContent,
        )
    }
}

@Composable
private fun DialRegion(
    state: QiblaState,
    style: QiblaStyle,
    strings: QiblaStrings,
    slots: QiblaSlots,
    modifier: Modifier,
) {
    val customDial = slots.compassDial
    if (customDial != null) {
        customDial(state)
    } else {
        QiblaCompassDial(
            state = state,
            style = style,
            strings = strings,
            modifier = modifier,
        )
    }
}

@Composable
private fun MessageRegion(
    state: QiblaState,
    controller: QiblaController,
    style: QiblaStyle,
    strings: QiblaStrings,
    slots: QiblaSlots,
) {
    val customMessage = slots.stateMessage
    if (customMessage != null) {
        customMessage(state)
    } else {
        QiblaStateMessage(state, style = style, strings = strings)
    }

    val customCalibration = slots.calibrationSheet
    if (state.uiState == QiblaUiState.CALIBRATION_NEEDED && customCalibration != null) {
        customCalibration(state, controller::dismissCalibration)
    }

    QiblaActionButtons(
        state = state,
        controller = controller,
        style = style,
        strings = strings,
        slots = slots,
    )
}

@Composable
private fun StatusRowsRegion(
    state: QiblaState,
    style: QiblaStyle,
    strings: QiblaStrings,
    slots: QiblaSlots,
) {
    val customRows = slots.statusRows
    if (customRows != null) {
        customRows(state)
    } else {
        QiblaStatusPanel(state, style = style, strings = strings)
    }
}
