package dev.hai.emojibattery.app

import android.content.pm.ActivityInfo
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
import dev.hai.emojibattery.locale.AppLocalePreferences
import dev.hai.emojibattery.ui.theme.EmojiBatteryTheme

class MainActivity : AppCompatActivity() {
    private val routeOverride = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        applyOrientationPolicy()
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

    override fun onResume() {
        super.onResume()
        applyOrientationPolicy()
    }

    private fun applyOrientationPolicy() {
        requestedOrientation =
            if (resources.configuration.smallestScreenWidthDp >= LARGE_SCREEN_SMALLEST_WIDTH_DP) {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
    }

    private companion object {
        const val LARGE_SCREEN_SMALLEST_WIDTH_DP = 600
    }
}
