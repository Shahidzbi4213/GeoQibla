package com.shahid.tech.qibla

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

class QiblaSlots(
    val topBar: (@Composable (QiblaState) -> Unit)? = null,
    val compassDial: (@Composable (QiblaState) -> Unit)? = null,
    val targetBadge: (@Composable (QiblaState) -> Unit)? = null,
    val statusRows: (@Composable (QiblaState) -> Unit)? = null,
    val stateMessage: (@Composable (QiblaState) -> Unit)? = null,
    val calibrationSheet: (@Composable (QiblaState, onDismiss: () -> Unit) -> Unit)? = null,
    val actionButtons: (@Composable RowScope.(QiblaState) -> Unit)? = null,
) {
    companion object {
        fun default(): QiblaSlots = QiblaSlots()
    }
}
