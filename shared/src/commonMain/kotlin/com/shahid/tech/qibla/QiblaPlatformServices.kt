package com.shahid.tech.qibla

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal interface QiblaPlatformServices {
    val locationAccess: StateFlow<QiblaLocationAccess>

    fun requestPermission()
    fun observeLocation(config: QiblaConfig): Flow<QiblaLocationSnapshot>
    fun observeOrientation(config: QiblaConfig): Flow<QiblaOrientationSnapshot>
    fun openLocationSettings()
    fun openAppSettings()
    fun performAlignmentHaptic()
}

internal data class QiblaLocationSnapshot(
    val access: QiblaLocationAccess,
    val fix: QiblaLocationFix?,
    val isLocationEnabled: Boolean,
    val isResolvingAddress: Boolean = false,
    val label: String? = fix?.addressLabel,
    val errorMessage: String? = null,
)

internal data class QiblaOrientationSnapshot(
    val azimuthDegrees: Float?,
    val azimuthReference: QiblaAzimuthReference,
    val accuracy: QiblaSensorAccuracy,
    val source: QiblaOrientationSource,
    val tiltDegrees: Float = 0f,
    val magneticFieldMicrotesla: Float? = null,
)

internal enum class QiblaAzimuthReference {
    MAGNETIC_NORTH,
    TRUE_NORTH,
}

@Composable
internal expect fun rememberQiblaPlatformServices(): QiblaPlatformServices
