package dev.hai.emojibattery.model

import co.q7labs.co.emoji.R

/**
 * Mirrors the original app's static list (`ColorTemplateLocalModel` in decompiled `C5914jz0.c`):
 * `background_template/template_color_01.png` … `template_color_20.png` under assets.
 *
 * The decompiled APK ships PNGs in assets; this clone uses bundled XML **shape** gradients for
 * [previewDrawableRes]. Compose [androidx.compose.ui.res.painterResource] cannot load those;
 * UI uses [android.widget.ImageView] (see `ThemeShapeDrawableImage` in Status Bar screens).
 */
data class BackgroundTemplateEntry(
    val index: Int,
    /** Same relative path as the original `ColorTemplateLocalModel` second constructor arg. */
    val assetRelativePath: String,
    /** In-app preview (gradient XML) — unique per row for the grid. */
    val previewDrawableRes: Int,
)

object StatusBarThemeTemplateCatalog {

    val entries: List<BackgroundTemplateEntry> = listOf(
        BackgroundTemplateEntry(0, "background_template/template_color_01.png", R.drawable.theme_bg_template_01),
        BackgroundTemplateEntry(1, "background_template/template_color_02.png", R.drawable.theme_bg_template_02),
        BackgroundTemplateEntry(2, "background_template/template_color_03.png", R.drawable.theme_bg_template_03),
        BackgroundTemplateEntry(3, "background_template/template_color_04.png", R.drawable.theme_bg_template_04),
        BackgroundTemplateEntry(4, "background_template/template_color_05.png", R.drawable.theme_bg_template_05),
        BackgroundTemplateEntry(5, "background_template/template_color_06.png", R.drawable.theme_bg_template_06),
        BackgroundTemplateEntry(6, "background_template/template_color_07.png", R.drawable.theme_bg_template_07),
        BackgroundTemplateEntry(7, "background_template/template_color_08.png", R.drawable.theme_bg_template_08),
        BackgroundTemplateEntry(8, "background_template/template_color_09.png", R.drawable.theme_bg_template_09),
        BackgroundTemplateEntry(9, "background_template/template_color_10.png", R.drawable.theme_bg_template_10),
        BackgroundTemplateEntry(10, "background_template/template_color_11.png", R.drawable.theme_bg_template_11),
        BackgroundTemplateEntry(11, "background_template/template_color_12.png", R.drawable.theme_bg_template_12),
        BackgroundTemplateEntry(12, "background_template/template_color_13.png", R.drawable.theme_bg_template_13),
        BackgroundTemplateEntry(13, "background_template/template_color_14.png", R.drawable.theme_bg_template_14),
        BackgroundTemplateEntry(14, "background_template/template_color_15.png", R.drawable.theme_bg_template_15),
        BackgroundTemplateEntry(15, "background_template/template_color_16.png", R.drawable.theme_bg_template_16),
        BackgroundTemplateEntry(16, "background_template/template_color_17.png", R.drawable.theme_bg_template_17),
        BackgroundTemplateEntry(17, "background_template/template_color_18.png", R.drawable.theme_bg_template_18),
        BackgroundTemplateEntry(18, "background_template/template_color_19.png", R.drawable.theme_bg_template_19),
        BackgroundTemplateEntry(19, "background_template/template_color_20.png", R.drawable.theme_bg_template_20),
    )
}
