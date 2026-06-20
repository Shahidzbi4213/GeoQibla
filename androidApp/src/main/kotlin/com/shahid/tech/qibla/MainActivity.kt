package com.shahid.tech.qibla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            GeoQiblaSample()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    GeoQiblaSample()
}

@Composable
private fun GeoQiblaSample() {
    MaterialTheme {
        Text("GeoQibla")
    }
}
