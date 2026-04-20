package dev.hai.emojibattery.model

import android.content.Context
import android.content.res.AssetManager
import org.json.JSONArray
import org.json.JSONObject

/**
 * Theme option model parsed from assets/theme_options/<pack>/themes.json.
 *
 * All image/file fields are relative paths under android assets, e.g.
 * `theme_options/xiyxiy/assets/wallpaper/w_0.jpg`.
 */
data class ThemeWallpaperConfig(
    val default: String,
    val variants: List<String>,
)

data class ThemeBatteryFrame(
    val width: Int,
    val height: Int,
)

data class ThemeBatteryMode(
    val asset: String,
    val frames: Int,
)

data class ThemeBatterySpriteSheet(
    val assetRoot: String,
    val type: String,
    val layout: String,
    val frame: ThemeBatteryFrame,
    val indexing: String,
    val progressMapping: String,
    val modes: Map<String, ThemeBatteryMode>,
)

data class ThemeOptionComponents(
    val wallpaper: ThemeWallpaperConfig,
    val lockScreen: String,
    val battery: ThemeBatterySpriteSheet?,
    val wifi: List<String>,
    val signal: List<String>,
    val bluetooth: List<String>,
    val charge: String,
    val data: List<String>,
    val hotspot: List<String>,
    val airplane: String,
)

data class ThemeOptionItem(
    val id: String,
    val name: String,
    val previewImage: String,
    val components: ThemeOptionComponents,
)

data class ThemeDefinition(
    val id: String,
    val name: String,
    val options: List<ThemeOptionItem>,
)

fun ThemeOptionComponents.wallpaperPreviewAsset(): String = wallpaper.default

fun ThemeOptionComponents.batteryPreviewAsset(): String =
    battery?.modes?.get("normal")?.asset
        ?: battery?.modes?.values?.firstOrNull()?.asset
        ?: charge

object ThemeOptionCatalog {
    private const val ROOT_ASSET_DIR = "theme_options"
    private val PACK_DIRS = listOf(
        "xiyxiy",
        "fujing",
        "morandi",
        "star_seeker",
        "the_time_of_the_wind",
        "tide_v2",
        "toonify",
        "trabas_v12",
        "trend_astronaut",
        "sur_mod_miui13_v1",
        "swift_ax",
        "technology_pro",
        "tenebre_v13",
        "the_astronaut",
    )
    private val IMAGE_EXTS = listOf("png", "jpg", "jpeg", "webp")

    fun assetUri(assetPath: String): String = "file:///android_asset/$assetPath"

    fun loadFromAssets(context: Context): List<ThemeDefinition> {
        val assets = context.assets
        val result = mutableListOf<ThemeDefinition>()
        PACK_DIRS.forEach { packDir ->
            val jsonPath = "$ROOT_ASSET_DIR/$packDir/themes.json"
            val jsonText = runCatching {
                assets.open(jsonPath).bufferedReader().use { it.readText() }
            }.getOrNull() ?: return@forEach
            result += parsePackJson(assets, packDir, jsonText)
        }
        return result
    }

    private fun parsePackJson(
        assets: AssetManager,
        packDir: String,
        jsonText: String,
    ): List<ThemeDefinition> {
        val root = JSONObject(jsonText)
        val themesJson = root.optJSONArray("themes") ?: return emptyList()
        val themes = mutableListOf<ThemeDefinition>()

        for (themeIndex in 0 until themesJson.length()) {
            val themeObject = themesJson.optJSONObject(themeIndex) ?: continue
            val rawThemeId = themeObject.optString("id").ifBlank { "theme_$themeIndex" }
            val scopedThemeId = "${packDir}__${rawThemeId}"
            val optionsJson = themeObject.optJSONArray("options")
            val options = mutableListOf<ThemeOptionItem>()

            if (optionsJson != null) {
                for (optionIndex in 0 until optionsJson.length()) {
                    val optionObject = optionsJson.optJSONObject(optionIndex) ?: continue
                    val rawOptionId = optionObject.optString("id").ifBlank { "option_$optionIndex" }
                    val componentsObject = optionObject.optJSONObject("components") ?: JSONObject()
                    val wallpaperConfig = parseWallpaperConfig(assets, packDir, componentsObject)

                    options += ThemeOptionItem(
                        id = "${scopedThemeId}__${rawOptionId}",
                        name = optionObject.optString("name"),
                        previewImage = resolveAssetPath(
                            assets = assets,
                            packDir = packDir,
                            componentDir = "wallpaper",
                            assetKey = optionObject.optString("previewImage"),
                        ).ifBlank { wallpaperConfig.default },
                        components = ThemeOptionComponents(
                            wallpaper = wallpaperConfig,
                            lockScreen = resolveAssetPath(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "lockscreen",
                                assetKey = componentsObject.optString("lockscreen"),
                            ),
                            battery = parseBatteryConfig(
                                assets = assets,
                                packDir = packDir,
                                componentsObject = componentsObject,
                            ),
                            wifi = resolveAssetArray(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "wifi",
                                componentsObject = componentsObject,
                                key = "wifi",
                            ),
                            signal = resolveAssetListOrSingle(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "signal",
                                componentsObject = componentsObject,
                                key = "signal",
                            ),
                            bluetooth = resolveAssetArray(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "bluetooth",
                                componentsObject = componentsObject,
                                key = "bluetooth",
                            ),
                            charge = resolveAssetPath(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "charge",
                                assetKey = componentsObject.optString("charge"),
                            ),
                            data = resolveAssetArray(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "data",
                                componentsObject = componentsObject,
                                key = "data",
                            ),
                            hotspot = resolveAssetListOrSingle(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "hotspot",
                                componentsObject = componentsObject,
                                key = "hotspot",
                            ),
                            airplane = resolveAssetPath(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "airplane",
                                assetKey = componentsObject.optString("airplane"),
                            ),
                        ),
                    )
                }
            }

            themes += ThemeDefinition(
                id = scopedThemeId,
                name = themeObject.optString("name"),
                options = options,
            )
        }
        return themes
    }

    private fun parseWallpaperConfig(
        assets: AssetManager,
        packDir: String,
        componentsObject: JSONObject,
    ): ThemeWallpaperConfig {
        val wallpaperNode = componentsObject.opt("wallpaper")
        if (wallpaperNode is JSONObject) {
            val defaultPath = resolveAssetPath(
                assets = assets,
                packDir = packDir,
                componentDir = "wallpaper",
                assetKey = wallpaperNode.optString("default"),
            )
            val variantsArray = wallpaperNode.optJSONArray("variants") ?: JSONArray()
            val variants = mutableListOf<String>()
            for (index in 0 until variantsArray.length()) {
                val raw = variantsArray.optString(index)
                variants += resolveAssetPath(
                    assets = assets,
                    packDir = packDir,
                    componentDir = "wallpaper",
                    assetKey = raw,
                )
            }
            val finalVariants = variants.filter { it.isNotBlank() }
            return ThemeWallpaperConfig(
                default = if (defaultPath.isNotBlank()) defaultPath else finalVariants.firstOrNull().orEmpty(),
                variants = if (finalVariants.isNotEmpty()) finalVariants else listOf(defaultPath).filter { it.isNotBlank() },
            )
        }

        val single = resolveAssetPath(
            assets = assets,
            packDir = packDir,
            componentDir = "wallpaper",
            assetKey = wallpaperNode?.toString().orEmpty(),
        )
        return ThemeWallpaperConfig(default = single, variants = listOf(single).filter { it.isNotBlank() })
    }

    private fun parseBatteryConfig(
        assets: AssetManager,
        packDir: String,
        componentsObject: JSONObject,
    ): ThemeBatterySpriteSheet? {
        val batteryNode = componentsObject.opt("battery") ?: return null

        if (batteryNode is JSONObject) {
            return parseBatterySpriteSheet(
                assets = assets,
                packDir = packDir,
                spriteObject = batteryNode,
                parentAssetRoot = "",
                fallbackAssetRoot = "assets/battery",
            )
        }

        if (batteryNode is JSONArray) {
            val fallback = mutableListOf<String>()
            for (index in 0 until batteryNode.length()) {
                fallback += resolveAssetPath(
                    assets = assets,
                    packDir = packDir,
                    componentDir = "battery",
                    assetKey = batteryNode.optString(index),
                )
            }
            val primary = fallback.firstOrNull().orEmpty()
            if (primary.isBlank()) return null
            return ThemeBatterySpriteSheet(
                assetRoot = "$ROOT_ASSET_DIR/$packDir/assets/battery",
                type = "legacy_list",
                layout = "unknown",
                frame = ThemeBatteryFrame(width = 0, height = 0),
                indexing = "unknown",
                progressMapping = "unknown",
                modes = mapOf(
                    "normal" to ThemeBatteryMode(asset = primary, frames = fallback.size.coerceAtLeast(1)),
                ),
            )
        }

        return null
    }

    private fun parseBatterySpriteSheet(
        assets: AssetManager,
        packDir: String,
        spriteObject: JSONObject,
        parentAssetRoot: String,
        fallbackAssetRoot: String,
    ): ThemeBatterySpriteSheet {
        val assetRoot = normalizeAssetRoot(
            parentAssetRoot = parentAssetRoot,
            rawAssetRoot = spriteObject.optString("assetRoot"),
            fallback = fallbackAssetRoot,
        )
        val frameObject = spriteObject.optJSONObject("frame")
        val frame = ThemeBatteryFrame(
            width = frameObject?.optInt("width") ?: 0,
            height = frameObject?.optInt("height") ?: 0,
        )
        val modesObject = spriteObject.optJSONObject("modes")
        val modes = linkedMapOf<String, ThemeBatteryMode>()
        if (modesObject != null) {
            val modeKeys = modesObject.keys()
            while (modeKeys.hasNext()) {
                val modeName = modeKeys.next()
                val modeJson = modesObject.optJSONObject(modeName) ?: continue
                modes[modeName] = ThemeBatteryMode(
                    asset = resolveAssetPathByRoot(
                        assets = assets,
                        packDir = packDir,
                        assetRoot = assetRoot,
                        assetKey = modeJson.optString("asset"),
                    ),
                    frames = modeJson.optInt("frames"),
                )
            }
        }

        return ThemeBatterySpriteSheet(
            assetRoot = buildAssetRootPath(packDir, assetRoot),
            type = spriteObject.optString("type"),
            layout = spriteObject.optString("layout"),
            frame = frame,
            indexing = spriteObject.optString("indexing"),
            progressMapping = spriteObject.optString("progressMapping"),
            modes = modes,
        )
    }

    private fun resolveAssetArray(
        assets: AssetManager,
        packDir: String,
        componentDir: String,
        componentsObject: JSONObject,
        key: String,
    ): List<String> {
        val array = componentsObject.optJSONArray(key) ?: return emptyList()
        val resolved = mutableListOf<String>()
        for (index in 0 until array.length()) {
            val assetKey = array.optString(index)
            resolved += resolveAssetPath(
                assets = assets,
                packDir = packDir,
                componentDir = componentDir,
                assetKey = assetKey,
            )
        }
        return resolved
    }

    private fun resolveAssetListOrSingle(
        assets: AssetManager,
        packDir: String,
        componentDir: String,
        componentsObject: JSONObject,
        key: String,
    ): List<String> {
        return when (val node = componentsObject.opt(key)) {
            is JSONArray -> {
                val resolved = mutableListOf<String>()
                for (index in 0 until node.length()) {
                    resolved += resolveAssetPath(
                        assets = assets,
                        packDir = packDir,
                        componentDir = componentDir,
                        assetKey = node.optString(index),
                    )
                }
                resolved.filter { it.isNotBlank() }
            }
            null -> emptyList()
            else -> listOf(
                resolveAssetPath(
                    assets = assets,
                    packDir = packDir,
                    componentDir = componentDir,
                    assetKey = node.toString(),
                ),
            ).filter { it.isNotBlank() }
        }
    }

    private fun resolveAssetPath(
        assets: AssetManager,
        packDir: String,
        componentDir: String,
        assetKey: String,
    ): String =
        resolveAssetPathByRoot(
            assets = assets,
            packDir = packDir,
            assetRoot = "assets/$componentDir",
            assetKey = assetKey,
        )

    private fun resolveAssetPathByRoot(
        assets: AssetManager,
        packDir: String,
        assetRoot: String,
        assetKey: String,
    ): String {
        if (assetKey.isBlank()) return ""
        val baseDir = buildAssetRootPath(packDir, assetRoot)
        val noExtCandidate = "$baseDir/$assetKey".replace("//", "/")
        if (assetExists(assets, noExtCandidate)) return noExtCandidate
        if (assetKey.contains('.')) return noExtCandidate
        IMAGE_EXTS.forEach { ext ->
            val withExt = "$baseDir/$assetKey.$ext".replace("//", "/")
            if (assetExists(assets, withExt)) return withExt
        }
        return noExtCandidate
    }

    private fun normalizeAssetRoot(
        parentAssetRoot: String,
        rawAssetRoot: String,
        fallback: String,
    ): String {
        val parent = parentAssetRoot.trim().trim('/').takeIf { it.isNotBlank() }.orEmpty()
        val raw = rawAssetRoot.trim().trim('/').takeIf { it.isNotBlank() }.orEmpty()
        val target = if (raw.isNotBlank()) raw else fallback.trim().trim('/')

        return when {
            target.startsWith("assets/") -> target
            parent.isNotBlank() -> "$parent/$target".trim('/')
            else -> "assets/$target".trim('/')
        }
    }

    private fun buildAssetRootPath(packDir: String, assetRoot: String): String {
        val normalized = assetRoot.trim().trim('/')
        return "$ROOT_ASSET_DIR/$packDir/$normalized".replace("//", "/")
    }

    private fun assetExists(assets: AssetManager, assetPath: String): Boolean =
        runCatching { assets.open(assetPath).use { } }.isSuccess
}
