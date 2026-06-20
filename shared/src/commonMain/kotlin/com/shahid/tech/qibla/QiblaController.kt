package com.shahid.tech.qibla

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface QiblaController {
    val state: StateFlow<QiblaState>

    fun start()
    fun stop()
    fun retryLocation()
    fun requestPermission()
    fun openLocationSettings()
    fun openAppSettings()
    fun dismissCalibration()
}

@Composable
fun rememberQiblaController(
    config: QiblaConfig = QiblaConfig(),
): QiblaController {
    val services = rememberQiblaPlatformServices()
    val scope = rememberCoroutineScope()
    val controller = remember(services, scope) {
        DefaultQiblaController(
            coroutineScope = scope,
            services = services,
            initialConfig = config,
        )
    }

    LaunchedEffect(controller, config) {
        controller.updateConfig(config)
    }
    DisposableEffect(controller) {
        onDispose { controller.stop() }
    }
    return controller
}
