package dev.hai.emojibattery.data

/**
 * Derives per-category PAD pack name for Home emoji catalog.
 *
 * Format: `home_pack_<category_uuid_with_underscores>`.
 */
object HomeCategoryPackResolver {
    fun packNameFor(categoryId: String): String =
        "home_pack_${categoryId.lowercase().replace('-', '_')}"
}

