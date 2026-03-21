package dev.hai.emojibattery.service

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import coil.load
import androidx.core.graphics.toColorInt

class StatusBarOverlayManager(
    private val context: Context,
) {
    data class LiveStatus(
        val batteryPercent: Int = 0,
        val charging: Boolean = false,
        val wifiEnabled: Boolean = false,
        val mobileConnected: Boolean = false,
        val airplaneMode: Boolean = false,
        val signalLevel: Int = 0,
    )

    private val windowManager = context.getSystemService(WindowManager::class.java)

    private val root = FrameLayout(context)
    private val statusRow = LinearLayout(context)
    private val leftCluster = LinearLayout(context)
    private val rightCluster = LinearLayout(context)
    private val clockView = TextClock(context)
    private val dateView = TextClock(context)
    private val wifiView = TextView(context)
    private val signalView = TextView(context)
    private val batteryView = TextView(context)
    private val stickerEmojiView = TextView(context)
    private val stickerImageView = ImageView(context)
    private val trollView = TextView(context)
    private val realtimeView = TextView(context)

    private var attached = false

    init {
        root.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        )
        statusRow.orientation = LinearLayout.HORIZONTAL
        statusRow.gravity = Gravity.CENTER_VERTICAL
        statusRow.setPadding(28, 10, 28, 10)

        leftCluster.orientation = LinearLayout.VERTICAL
        leftCluster.gravity = Gravity.CENTER_VERTICAL

        clockView.textSize = 13f
        clockView.format12Hour = "HH:mm"
        clockView.format24Hour = "HH:mm"
        clockView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        dateView.textSize = 10f
        dateView.format12Hour = "EEE, MMM d"
        dateView.format24Hour = "EEE, MMM d"
        dateView.alpha = 0.75f
        leftCluster.addView(clockView)
        leftCluster.addView(dateView)
        val left = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        statusRow.addView(leftCluster, left)

        rightCluster.orientation = LinearLayout.HORIZONTAL
        rightCluster.gravity = Gravity.CENTER_VERTICAL
        batteryView.textSize = 13f
        batteryView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        wifiView.textSize = 11f
        signalView.textSize = 11f
        wifiView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        signalView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        signalView.setPadding(12, 0, 0, 0)
        batteryView.setPadding(12, 0, 0, 0)
        rightCluster.addView(wifiView)
        rightCluster.addView(signalView)
        rightCluster.addView(batteryView)
        statusRow.addView(rightCluster)

        root.addView(statusRow)
        val stickerLp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply {
            topMargin = 64
        }
        stickerImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        stickerImageView.adjustViewBounds = true
        val stickerMax = (56 * context.resources.displayMetrics.density).toInt()
        stickerImageView.maxWidth = stickerMax
        stickerImageView.maxHeight = stickerMax
        root.addView(stickerImageView, FrameLayout.LayoutParams(stickerLp))
        root.addView(stickerEmojiView, FrameLayout.LayoutParams(stickerLp))
        root.addView(trollView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.END).apply {
            topMargin = 64
            marginEnd = 24
        })
        root.addView(realtimeView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START).apply {
            topMargin = 64
            marginStart = 24
        })

        listOf(stickerEmojiView, trollView, realtimeView).forEach { view ->
            view.textSize = 18f
            view.setPadding(18, 10, 18, 10)
            view.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
        }
        stickerImageView.setPadding(18, 10, 18, 10)
    }

    fun render(snapshot: OverlaySnapshot, liveStatus: LiveStatus) {
        ensureAttached()
        if (!snapshot.statusBarEnabled && !snapshot.stickerEnabled && !snapshot.trollEnabled && !snapshot.realTimeEnabled) {
            root.alpha = 0f
            return
        }
        root.alpha = 1f
        statusRow.setBackgroundColor(snapshot.backgroundColor.toInt())
        batteryView.text = "${snapshot.batteryText} ${liveStatus.batteryPercent}%${if (liveStatus.charging) " +" else ""}"
        batteryView.setTextColor(snapshot.accentColor.toInt())
        clockView.setTextColor("#111111".toColorInt())
        dateView.setTextColor("#555555".toColorInt())
        wifiView.text = when {
            liveStatus.airplaneMode -> "AIR"
            liveStatus.wifiEnabled -> "WIFI"
            liveStatus.mobileConnected -> "LTE"
            else -> "OFF"
        }
        signalView.text = if (liveStatus.airplaneMode) "" else signalGlyph(liveStatus.signalLevel)
        wifiView.setTextColor("#333333".toColorInt())
        signalView.setTextColor("#333333".toColorInt())

        if (snapshot.stickerEnabled) {
            val url = snapshot.stickerThumbnailUrl?.takeIf { it.isNotBlank() }
            if (url != null) {
                stickerImageView.visibility = View.VISIBLE
                stickerEmojiView.visibility = View.GONE
                stickerImageView.load(url) {
                    crossfade(true)
                }
            } else {
                stickerImageView.visibility = View.GONE
                stickerEmojiView.visibility = View.VISIBLE
                stickerEmojiView.text = snapshot.stickerGlyph
            }
        } else {
            stickerImageView.visibility = View.GONE
            stickerEmojiView.visibility = View.GONE
        }

        trollView.text = "Fake ${snapshot.trollMessage}"
        trollView.visibility = if (snapshot.trollEnabled) View.VISIBLE else View.GONE
        trollView.setBackgroundColor("#FFF2D9".toColorInt())

        realtimeView.text = "${snapshot.realTimeGlyph} ${snapshot.realTimeTitle}"
        realtimeView.visibility = if (snapshot.realTimeEnabled) View.VISIBLE else View.GONE
        realtimeView.setBackgroundColor("#E7F0FF".toColorInt())

        statusRow.visibility = if (snapshot.statusBarEnabled) View.VISIBLE else View.GONE
    }

    fun detach() {
        if (!attached) return
        windowManager.removeView(root)
        attached = false
    }

    private fun ensureAttached() {
        if (attached) return
        windowManager.addView(root, createLayoutParams())
        attached = true
    }

    private fun signalGlyph(level: Int): String = when (level.coerceIn(0, 4)) {
        0 -> "▱▱▱▱"
        1 -> "▰▱▱▱"
        2 -> "▰▰▱▱"
        3 -> "▰▰▰▱"
        else -> "▰▰▰▰"
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP
        }
    }
}
