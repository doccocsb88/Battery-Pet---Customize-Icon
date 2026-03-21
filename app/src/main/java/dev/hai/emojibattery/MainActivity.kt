package dev.hai.emojibattery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.hai.emojibattery.app.EmojiBatteryApp
import dev.hai.emojibattery.app.EmojiBatteryViewModel
import dev.hai.emojibattery.ui.theme.EmojiBatteryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialRoute = intent.getStringExtra("route")
        setContent {
            EmojiBatteryTheme {
                val viewModel: EmojiBatteryViewModel = viewModel()
                EmojiBatteryApp(
                    viewModel = viewModel,
                    initialRoute = initialRoute,
                )
            }
        }
    }
}
