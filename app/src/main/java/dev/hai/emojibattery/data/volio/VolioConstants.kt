package dev.hai.emojibattery.data.volio

/**
 * Same app scope id as decompiled [hungvv.OS] (emoji battery store integration).
 * Base URL matches decompiled [hungvv.C2984On.f] (base64-decoded), host is **stores** (plural).
 */
object VolioConstants {
    const val PUBLIC_API_BASE = "https://stores.volio.vn/stores/api/v5.0/public/"
    const val PARENT_APP_ID = "26bf9d75-7fd5-438d-81b0-b901f5ba2cd5"
    /** Sticker store scope from decompiled [hungvv.GT] (Emoji sticker Volio parent). */
    const val STICKER_PARENT_ID = "9f7b1b47-ee3f-4bf1-b857-09f4c73ffbf0"
    /**
     * Optional Volio scope for Battery Troll templates (set from decompiled original when known).
     * Empty string disables remote fetch; UI uses [SampleCatalog.batteryTrollTemplates] only.
     */
    const val BATTERY_TROLL_PARENT_ID = ""
    const val ITEM_PAGE_SIZE = 500
}
