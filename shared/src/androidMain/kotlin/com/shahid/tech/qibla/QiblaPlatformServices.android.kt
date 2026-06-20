package com.shahid.tech.qibla

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

@Composable
internal actual fun rememberQiblaPlatformServices(): QiblaPlatformServices =
    remember { AndroidQiblaPlatformServices() }

internal actual fun currentTimeMillis(): Long = System.currentTimeMillis()

private class AndroidQiblaPlatformServices : QiblaPlatformServices {
    override val locationAccess = MutableStateFlow(QiblaLocationAccess.NOT_DETERMINED)

    override fun requestPermission() {
        locationAccess.value = QiblaLocationAccess.DENIED
    }

    override fun observeLocation(config: QiblaConfig): Flow<QiblaLocationSnapshot> =
        flowOf(
            QiblaLocationSnapshot(
                access = locationAccess.value,
                fix = null,
                isLocationEnabled = true,
            ),
        )

    override fun observeOrientation(config: QiblaConfig): Flow<QiblaOrientationSnapshot> =
        flowOf(
            QiblaOrientationSnapshot(
                azimuthDegrees = null,
                azimuthReference = QiblaAzimuthReference.TRUE_NORTH,
                accuracy = QiblaSensorAccuracy.UNAVAILABLE,
                source = QiblaOrientationSource.NONE,
            ),
        )

    override fun openLocationSettings() = Unit
    override fun openAppSettings() = Unit
    override fun performAlignmentHaptic() = Unit
}
