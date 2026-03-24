package dev.hai.emojibattery.service

import android.view.Gravity
import co.q7labs.co.emoji.R

/**
 * Mirrors the original app's notch template IDs and drawable mapping.
 */
data class NotchTemplate(
    val id: Int,
    val drawableRes: Int?,
    val gravity: Int = Gravity.TOP or Gravity.CENTER_HORIZONTAL,
)

object NotchTemplateCatalog {
    private val entries = listOf(
        NotchTemplate(id = -1, drawableRes = null),
        NotchTemplate(id = 1, drawableRes = R.drawable.notch1),
        NotchTemplate(id = 2, drawableRes = R.drawable.notch2),
        NotchTemplate(id = 3, drawableRes = R.drawable.notch3),
        NotchTemplate(id = 4, drawableRes = R.drawable.notch4),
        NotchTemplate(id = 5, drawableRes = R.drawable.notch5),
        NotchTemplate(id = 6, drawableRes = R.drawable.notch6),
        NotchTemplate(id = 7, drawableRes = R.drawable.notch7),
        NotchTemplate(id = 8, drawableRes = R.drawable.notch_dynamic1, gravity = Gravity.CENTER),
        NotchTemplate(id = 9, drawableRes = R.drawable.notch_dynamic2, gravity = Gravity.CENTER),
        NotchTemplate(id = 10, drawableRes = R.drawable.notch_dynamic3, gravity = Gravity.CENTER),
        NotchTemplate(id = 11, drawableRes = R.drawable.notch_dynamic4, gravity = Gravity.CENTER),
        NotchTemplate(id = 12, drawableRes = R.drawable.notch_dynamic5, gravity = Gravity.CENTER),
        NotchTemplate(id = 13, drawableRes = R.drawable.notch_dynamic6, gravity = Gravity.CENTER),
    )

    fun resolve(id: Int): NotchTemplate = entries.firstOrNull { it.id == id } ?: entries.first()
}
