package dev.hai.emojibattery.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.BatteryManager
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.getSystemService

class OverlayAccessibilityService : AccessibilityService() {
    private lateinit var overlayManager: StatusBarOverlayManager
    private var batteryPercent: Int = 0
    private var charging: Boolean = false
    private var signalLevel: Int = 0
    private var refreshRegistered = false
    private var batteryRegistered = false
    private var connectivityRegistered = false
    private var timeRegistered = false

    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshOverlay()
        }
    }
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
            batteryPercent = ((level * 100f) / scale).toInt()
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            refreshOverlay()
        }
    }
    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshOverlay()
        }
    }
    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshOverlay()
        }
    }
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            signalLevel = signalStrength.level
            refreshOverlay()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = StatusBarOverlayManager(this)
        registerRefreshReceiver()
        registerBatteryReceiver()
        registerConnectivityReceiver()
        registerTimeReceiver()
        registerSignalListener()
        refreshOverlay()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            refreshOverlay()
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        safeUnregister(refreshRegistered, refreshReceiver)
        safeUnregister(batteryRegistered, batteryReceiver)
        safeUnregister(connectivityRegistered, connectivityReceiver)
        safeUnregister(timeRegistered, timeReceiver)
        getSystemService<TelephonyManager>()?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        overlayManager.detach()
        super.onDestroy()
    }

    private fun registerRefreshReceiver() {
        val filter = IntentFilter(ACTION_REFRESH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(refreshReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(refreshReceiver, filter)
        }
        refreshRegistered = true
    }

    private fun refreshOverlay() {
        overlayManager.render(
            OverlayConfigStore.read(this),
            StatusBarOverlayManager.LiveStatus(
                batteryPercent = batteryPercent,
                charging = charging,
                wifiEnabled = isWifiEnabled(),
                mobileConnected = isMobileConnected(),
                airplaneMode = isAirplaneModeOn(),
                signalLevel = signalLevel,
            ),
        )
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
        batteryRegistered = true
    }

    private fun registerConnectivityReceiver() {
        val filter = IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction("android.net.wifi.WIFI_AP_STATE_CHANGED")
            addAction("android.net.wifi.WIFI_STATE_CHANGED")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(connectivityReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(connectivityReceiver, filter)
        }
        connectivityRegistered = true
    }

    private fun registerTimeReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timeReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(timeReceiver, filter)
        }
        timeRegistered = true
    }

    private fun registerSignalListener() {
        getSystemService<TelephonyManager>()?.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    private fun isWifiEnabled(): Boolean {
        val connectivityManager = getSystemService<ConnectivityManager>() ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun isMobileConnected(): Boolean {
        val connectivityManager = getSystemService<ConnectivityManager>() ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private fun isAirplaneModeOn(): Boolean {
        return Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
    }

    private fun safeUnregister(registered: Boolean, receiver: BroadcastReceiver) {
        if (!registered) return
        runCatching { unregisterReceiver(receiver) }
    }

    companion object {
        const val ACTION_REFRESH = "dev.hai.emojibattery.action.REFRESH_OVERLAY"

        fun requestRefresh(context: Context) {
            context.sendBroadcast(Intent(ACTION_REFRESH).setPackage(context.packageName))
        }
    }
}
