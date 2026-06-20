package com.shahid.tech.qibla

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class QiblaStyle(
    val colors: QiblaColors = QiblaColors(),
    val dimensions: QiblaDimensions = QiblaDimensions(),
    val typography: QiblaTypography = QiblaTypography(),
    val shapes: QiblaShapes = QiblaShapes(),
    val animation: QiblaAnimationTimings = QiblaAnimationTimings(),
) {
    companion object {
        fun default(): QiblaStyle = QiblaStyle()
    }
}

data class QiblaColors(
    val background: Color = Color(0xFFFAFAF7),
    val surface: Color = Color(0xFFFFFFFF),
    val surfaceVariant: Color = Color(0xFFF0F3EE),
    val content: Color = Color(0xFF202523),
    val secondaryContent: Color = Color(0xFF5D6662),
    val outline: Color = Color(0xFFD7DDD8),
    val primary: Color = Color(0xFF096B58),
    val onPrimary: Color = Color(0xFFFFFFFF),
    val qibla: Color = Color(0xFF0A7C66),
    val near: Color = Color(0xFFB87900),
    val aligned: Color = Color(0xFF167D38),
    val warning: Color = Color(0xFF9C4A00),
    val error: Color = Color(0xFFB3261E),
    val compassTick: Color = Color(0xFF7B8580),
)

data class QiblaDimensions(
    val screenPadding: Dp = 20.dp,
    val compactScreenPadding: Dp = 16.dp,
    val contentGap: Dp = 16.dp,
    val panelPadding: Dp = 16.dp,
    val compassMinSize: Dp = 240.dp,
    val compassMaxSize: Dp = 360.dp,
    val statusRowMinHeight: Dp = 44.dp,
)

data class QiblaTypography(
    val title: TextStyle = TextStyle(
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    val subtitle: TextStyle = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    val statusTitle: TextStyle = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    val body: TextStyle = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    val label: TextStyle = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
    val numeric: TextStyle = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)

data class QiblaShapes(
    val panel: Shape = RoundedCornerShape(8.dp),
    val button: Shape = RoundedCornerShape(6.dp),
    val badge: Shape = RoundedCornerShape(8.dp),
)

data class QiblaAnimationTimings(
    val compassRotationMillis: Int = 220,
    val stateChangeMillis: Int = 180,
)
