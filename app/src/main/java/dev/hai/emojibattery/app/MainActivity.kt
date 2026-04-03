package dev.hai.emojibattery.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.hai.emojibattery.app.EmojiBatteryApp
import dev.hai.emojibattery.app.EmojiBatteryViewModel
import dev.hai.emojibattery.locale.AppLocalePreferences
import dev.hai.emojibattery.ui.theme.EmojiBatteryTheme

class MainActivity : AppCompatActivity() {
    private val routeOverride = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        AppLocalePreferences.applyAppLocalesAtStartup(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.Transparent.toArgb(),
                darkScrim = Color.Transparent.toArgb(),
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.Transparent.toArgb(),
                darkScrim = Color.Transparent.toArgb(),
            ),
        )
        routeOverride.value = intent.getStringExtra("route")
        setContent {
            EmojiBatteryTheme {
                val viewModel: EmojiBatteryViewModel = viewModel()
                EmojiBatteryApp(
                    viewModel = viewModel,
                    initialRoute = routeOverride.value,
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        routeOverride.value = intent.getStringExtra("route")
    }
}
