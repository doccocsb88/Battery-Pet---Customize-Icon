package dev.hai.emojibattery.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings

object AccessibilityBridge {
    fun isEnabled(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ).orEmpty()
        val expectedFlat = ComponentName(context, OverlayAccessibilityService::class.java).flattenToString()
        val expectedClass = OverlayAccessibilityService::class.java.name
        return enabledServices.split(':').any { service ->
            service.equals(expectedFlat, ignoreCase = true) || service.contains(expectedClass, ignoreCase = true)
        }
    }

    fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
