package dev.hai.emojibattery.locale

import android.content.Context
import co.q7labs.co.emoji.R
import java.util.Locale

data class SupportedAppLanguage(
    val localeTag: String,
    val flagResId: Int,
)

/**
 * Same order as the original language picker; [localeTag] is BCP 47 for
 * [Locale.forLanguageTag] / AppCompat application locales.
 */
val SUPPORTED_APP_LANGUAGES: List<SupportedAppLanguage> = listOf(
    SupportedAppLanguage("en", R.drawable.flag_united_kingdom),
    SupportedAppLanguage("hi", R.drawable.flag_india),
    SupportedAppLanguage("es", R.drawable.flag_spain),
    SupportedAppLanguage("fr", R.drawable.flag_france),
    SupportedAppLanguage("ar", R.drawable.flag_saudi_arabia),
    SupportedAppLanguage("pt-BR", R.drawable.flag_portugal),
    SupportedAppLanguage("id", R.drawable.flag_indonesia),
    SupportedAppLanguage("de", R.drawable.flag_germany),
    SupportedAppLanguage("vi", R.drawable.flag_vietnam),
    SupportedAppLanguage("ru", R.drawable.flag_russia),
    SupportedAppLanguage("ja", R.drawable.flag_japan),
    SupportedAppLanguage("ko", R.drawable.flag_south_korea),
    SupportedAppLanguage("tl", R.drawable.flag_philippines),
    SupportedAppLanguage("uz", R.drawable.flag_uzbekistn),
    SupportedAppLanguage("fa", R.drawable.flag_partian),
    SupportedAppLanguage("zh-CN", R.drawable.flag_china),
    SupportedAppLanguage("th", R.drawable.flag_thailand),
    SupportedAppLanguage("tr", R.drawable.flag_turkey),
)

fun localeForSupportedTag(tag: String): Locale =
    Locale.forLanguageTag(tag.replace('_', '-'))

fun resolveDefaultLocaleTag(context: Context): String {
    val saved = AppLocalePreferences.getPersistedLocaleTag(context)
    if (saved != null) return normalizeSupportedTag(saved)
    val system = Locale.getDefault()
    val systemTag = system.toLanguageTag().replace('_', '-')
    SUPPORTED_APP_LANGUAGES.firstOrNull { it.localeTag.equals(systemTag, ignoreCase = true) }
        ?.let { return it.localeTag }
    SUPPORTED_APP_LANGUAGES.firstOrNull {
        it.localeTag.startsWith(system.language, ignoreCase = true) ||
            localeForSupportedTag(it.localeTag).language.equals(system.language, ignoreCase = true)
    }?.let { return it.localeTag }
    return "en"
}

fun normalizeSupportedTag(tag: String): String {
    val normalized = tag.replace('_', '-')
    if (normalized.equals("in", ignoreCase = true)) return "id"
    if (normalized.equals("pt", ignoreCase = true)) return "pt-BR"
    if (normalized.equals("zh", ignoreCase = true)) return "zh-CN"
    if (normalized.equals("fil", ignoreCase = true)) return "tl"
    SUPPORTED_APP_LANGUAGES.firstOrNull { it.localeTag.equals(normalized, ignoreCase = true) }
        ?.let { return it.localeTag }
    return normalized
}

fun displayNameForLocaleTag(tag: String, displayLocale: Locale): String {
    val locale = localeForSupportedTag(tag)
    return locale.getDisplayName(displayLocale).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(displayLocale) else it.toString()
    }
}
