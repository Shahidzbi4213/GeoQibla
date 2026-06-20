package com.shahid.tech.qibla

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    val controller = rememberQiblaController()
    GeoQiblaScreen(controller = controller)
}
