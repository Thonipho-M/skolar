package com.example.skolar20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.skolar20.ui.App              // <-- import the Auth gate composable
import com.example.skolar20.ui.theme.Skolar20Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Skolar20Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    App()

                }
            }
        }
    }
}
