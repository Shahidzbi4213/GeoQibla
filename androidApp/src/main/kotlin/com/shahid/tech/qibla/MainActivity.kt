package com.shahid.tech.qibla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            GeoQiblaSampleApp()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    GeoQiblaSampleApp()
}

@Composable
private fun GeoQiblaSampleApp() {
    val controller = rememberQiblaController()
    GeoQiblaScreen(controller = controller)
}
