package com.example.furnexa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.furnexa.ui.navigation.FurnexaNavGraph
import com.example.furnexa.ui.theme.FurnexaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FurnexaTheme {
                FurnexaNavGraph()
            }
        }
    }
}
