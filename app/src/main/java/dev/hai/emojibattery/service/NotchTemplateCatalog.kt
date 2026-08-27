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
        NotchTemplate(id = 1, drawableRes = R.drawable.notch_es),
        NotchTemplate(id = 2, drawableRes = R.drawable.notch_hole),
        NotchTemplate(id = 3, drawableRes = R.drawable.notch_ipx),
        NotchTemplate(id = 4, drawableRes = R.drawable.notch_op6),
        NotchTemplate(id = 5, drawableRes = R.drawable.notch_op7),
        NotchTemplate(id = 6, drawableRes = R.drawable.notch_pix),
        NotchTemplate(id = 7, drawableRes = R.drawable.notch_punch),
        NotchTemplate(id = 8, drawableRes = R.drawable.notch_spun),
        NotchTemplate(id = 9, drawableRes = R.drawable.notch_i14_pro),
        NotchTemplate(id = 10, drawableRes = R.drawable.notch_i14_pro_black),
        NotchTemplate(id = 11, drawableRes = R.drawable.notch_arc),
        NotchTemplate(id = 12, drawableRes = R.drawable.notch_cap),
        NotchTemplate(id = 13, drawableRes = R.drawable.notch_man),
        NotchTemplate(id = 14, drawableRes = R.drawable.notch_reactor),
        NotchTemplate(id = 15, drawableRes = R.drawable.notch_win),
        NotchTemplate(id = 16, drawableRes = R.drawable.notch_wond),
        NotchTemplate(id = 17, drawableRes = R.drawable.notch_wond2),
        NotchTemplate(id = 18, drawableRes = R.drawable.notch_spidy),
        NotchTemplate(id = 19, drawableRes = R.drawable.notch_supe),
        NotchTemplate(id = 20, drawableRes = R.drawable.notch_puni),
        NotchTemplate(id = 21, drawableRes = R.drawable.notch_bat_r),
        NotchTemplate(id = 22, drawableRes = R.drawable.notch_bat),
        NotchTemplate(id = 23, drawableRes = R.drawable.notch_bat_1),
        NotchTemplate(id = 24, drawableRes = R.drawable.notch_bat_2),
        NotchTemplate(id = 25, drawableRes = R.drawable.notch_bat_3),
        NotchTemplate(id = 26, drawableRes = R.drawable.notch_bat_4),
        NotchTemplate(id = 27, drawableRes = R.drawable.notch_bat_5),
        NotchTemplate(id = 28, drawableRes = R.drawable.notch_bat_6),
        NotchTemplate(id = 29, drawableRes = R.drawable.notch_bat_7),
        NotchTemplate(id = 30, drawableRes = R.drawable.notch_bat_8),
        NotchTemplate(id = 31, drawableRes = R.drawable.notch_bat_9),
        NotchTemplate(id = 32, drawableRes = R.drawable.notch_bat_10),
        NotchTemplate(id = 33, drawableRes = R.drawable.notch_eagle),
        NotchTemplate(id = 34, drawableRes = R.drawable.notch_fire),
        NotchTemplate(id = 35, drawableRes = R.drawable.notch_hair),
        NotchTemplate(id = 36, drawableRes = R.drawable.notch_lineart),
        NotchTemplate(id = 37, drawableRes = R.drawable.notch_mustach),
        NotchTemplate(id = 38, drawableRes = R.drawable.notch_paw),
        NotchTemplate(id = 39, drawableRes = R.drawable.notch_pumkin),
        NotchTemplate(id = 40, drawableRes = R.drawable.notch_seq),
        NotchTemplate(id = 41, drawableRes = R.drawable.notch_trin),
        NotchTemplate(id = 42, drawableRes = R.drawable.notch_pill),
        NotchTemplate(id = 43, drawableRes = R.drawable.notch_oval),
        NotchTemplate(id = 44, drawableRes = R.drawable.notch_fire2),
        NotchTemplate(id = 45, drawableRes = R.drawable.notch_circle1),
        NotchTemplate(id = 46, drawableRes = R.drawable.notch_circle2),
        NotchTemplate(id = 47, drawableRes = R.drawable.notch_circle3),
        NotchTemplate(id = 48, drawableRes = R.drawable.notch_circle4),
        NotchTemplate(id = 49, drawableRes = R.drawable.notch_circle5),
        NotchTemplate(id = 50, drawableRes = R.drawable.notch_circle6),
        NotchTemplate(id = 51, drawableRes = R.drawable.notch_circle7),
        NotchTemplate(id = 52, drawableRes = R.drawable.notch_circle8),
        NotchTemplate(id = 53, drawableRes = R.drawable.notch_circle9),
        NotchTemplate(id = 54, drawableRes = R.drawable.notch_circle10),
        NotchTemplate(id = 55, drawableRes = R.drawable.notch_circle11),
        NotchTemplate(id = 56, drawableRes = R.drawable.notch_circle12),
        NotchTemplate(id = 57, drawableRes = R.drawable.notch_circle13),
        NotchTemplate(id = 58, drawableRes = R.drawable.notch_punch1),
        NotchTemplate(id = 59, drawableRes = R.drawable.notch_punch2),
        NotchTemplate(id = 60, drawableRes = R.drawable.notch_punch3),
        NotchTemplate(id = 61, drawableRes = R.drawable.notch_punch4),
        NotchTemplate(id = 62, drawableRes = R.drawable.notch_punch5),
        NotchTemplate(id = 63, drawableRes = R.drawable.notch_punch6),
        NotchTemplate(id = 64, drawableRes = R.drawable.notch_punch7),
        NotchTemplate(id = 65, drawableRes = R.drawable.notch_punch8),
    )

    fun resolve(id: Int): NotchTemplate = entries.firstOrNull { it.id == id } ?: entries.first()
    fun allTemplates(): List<NotchTemplate> = entries
}
