package dev.hai.emojibattery.model

import android.content.Context
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
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
    val ringer: List<String>,
    val airplane: String,
)

data class ThemeStatusIcons(
    val optionId: String,
    val statusBarBackgroundType: String,
    val statusBarColor: String,
    val wallpaper: String,
    val wifi: List<String>,
    val signal: List<String>,
    val data: List<String>,
    val hotspot: List<String>,
    val ringer: List<String>,
    val bluetooth: List<String>,
    val airplane: String,
    val charge: String,
    val battery: ThemeBatteryRuntime?,
)

data class ThemeBatteryRuntime(
    val normalAsset: String,
    val normalFrames: Int,
    val chargingAsset: String?,
    val chargingFrames: Int,
    val indexing: String,
    val frameWidth: Int,
    val frameHeight: Int,
)

data class ThemeOptionItem(
    val id: String,
    val name: String,
    val previewImage: String,
    val statusBarBackgroundType: String,
    val statusBarColor: String,
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

fun ThemeOptionItem.toThemeStatusIcons(): ThemeStatusIcons {
    fun List<String>.normalized(): List<String> = filter { it.isNotBlank() }.distinct()
    val batterySprite = components.battery
    val normalMode = batterySprite?.modes?.get("normal")
        ?: batterySprite?.modes?.values?.firstOrNull()
    val chargingMode = batterySprite?.modes?.get("charging")
    val batteryRuntime = if (batterySprite != null && normalMode != null && normalMode.asset.isNotBlank()) {
        ThemeBatteryRuntime(
            normalAsset = normalMode.asset.trim(),
            normalFrames = normalMode.frames.coerceAtLeast(1),
            chargingAsset = chargingMode?.asset?.trim()?.takeIf { it.isNotBlank() },
            chargingFrames = chargingMode?.frames?.coerceAtLeast(1) ?: normalMode.frames.coerceAtLeast(1),
            indexing = batterySprite.indexing.trim().ifBlank { "top_to_bottom" },
            frameWidth = batterySprite.frame.width.coerceAtLeast(0),
            frameHeight = batterySprite.frame.height.coerceAtLeast(0),
        )
    } else {
        null
    }

    return ThemeStatusIcons(
        optionId = id,
        statusBarBackgroundType = statusBarBackgroundType.trim().ifBlank { "bluebackground" },
        statusBarColor = statusBarColor.trim(),
        wallpaper = components.wallpaperPreviewAsset().ifBlank { previewImage }.trim(),
        wifi = components.wifi.normalized(),
        signal = components.signal.normalized(),
        data = components.data.normalized(),
        hotspot = components.hotspot.normalized(),
        ringer = components.ringer.normalized(),
        bluetooth = components.bluetooth.normalized(),
        airplane = components.airplane.trim(),
        charge = components.charge.trim(),
        battery = batteryRuntime,
    )
}

object ThemeOptionCatalog {
    private const val TAG = "ThemeSpriteValidator"
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
    @Volatile
    private var spriteValidationDone = false
    private val spriteValidationLock = Any()

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
        validateBatterySpriteConfigsOnce(assets, result)
        return result
    }

    private fun validateBatterySpriteConfigsOnce(
        assets: AssetManager,
        themes: List<ThemeDefinition>,
    ) {
        if (spriteValidationDone) return
        synchronized(spriteValidationLock) {
            if (spriteValidationDone) return
            val boundsCache = mutableMapOf<String, Pair<Int, Int>?>()
            val issues = mutableListOf<String>()
            var checkedModes = 0
            themes.forEach { theme ->
                theme.options.forEach { option ->
                    val battery = option.components.battery ?: return@forEach
                    val indexing = battery.indexing.trim().lowercase().ifBlank { "top_to_bottom" }
                    battery.modes.forEach { (modeName, mode) ->
                        val asset = mode.asset.trim()
                        if (asset.isBlank()) return@forEach
                        val bounds = boundsCache.getOrPut(asset) {
                            decodeAssetBounds(assets, asset)
                        }
                        if (bounds == null) {
                            issues += "missing asset theme=${theme.id} option=${option.id} mode=$modeName asset=$asset"
                            return@forEach
                        }
                        checkedModes += 1
                        val (width, height) = bounds
                        val frames = mode.frames.coerceAtLeast(1)
                        val cfgFrameWidth = battery.frame.width.coerceAtLeast(0)
                        val cfgFrameHeight = battery.frame.height.coerceAtLeast(0)

                        if (frames > 1) {
                            when (indexing) {
                                "left_to_right" -> {
                                    if (width % frames != 0) {
                                        issues += "non-divisible width theme=${theme.id} option=${option.id} mode=$modeName asset=$asset size=${width}x$height frames=$frames"
                                    }
                                    val inferredFrameWidth = (width / frames).coerceAtLeast(1)
                                    if (cfgFrameWidth > 0 && cfgFrameWidth != inferredFrameWidth) {
                                        issues += "frameWidth mismatch theme=${theme.id} option=${option.id} mode=$modeName asset=$asset cfg=$cfgFrameWidth inferred=$inferredFrameWidth width=$width frames=$frames"
                                    }
                                    if (cfgFrameHeight > 0 && cfgFrameHeight != height) {
                                        issues += "frameHeight mismatch-horizontal theme=${theme.id} option=${option.id} mode=$modeName asset=$asset cfg=$cfgFrameHeight imageHeight=$height"
                                    }
                                }

                                else -> {
                                    if (height % frames != 0) {
                                        issues += "non-divisible height theme=${theme.id} option=${option.id} mode=$modeName asset=$asset size=${width}x$height frames=$frames"
                                    }
                                    val inferredFrameHeight = (height / frames).coerceAtLeast(1)
                                    if (cfgFrameHeight > 0 && cfgFrameHeight != inferredFrameHeight) {
                                        issues += "frameHeight mismatch theme=${theme.id} option=${option.id} mode=$modeName asset=$asset cfg=$cfgFrameHeight inferred=$inferredFrameHeight height=$height frames=$frames"
                                    }
                                    if (cfgFrameWidth > 0 && cfgFrameWidth != width) {
                                        issues += "frameWidth mismatch-vertical theme=${theme.id} option=${option.id} mode=$modeName asset=$asset cfg=$cfgFrameWidth imageWidth=$width"
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (issues.isEmpty()) {
                Log.i(TAG, "validateBatterySpriteConfigs checkedModes=$checkedModes issues=0")
            } else {
                Log.w(TAG, "validateBatterySpriteConfigs checkedModes=$checkedModes issues=${issues.size}")
                issues.forEach { issue ->
                    Log.w(TAG, "batterySpriteIssue $issue")
                }
            }
            spriteValidationDone = true
        }
    }

    private fun decodeAssetBounds(
        assets: AssetManager,
        assetPath: String,
    ): Pair<Int, Int>? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        return runCatching {
            assets.open(assetPath).use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            if (options.outWidth > 0 && options.outHeight > 0) {
                options.outWidth to options.outHeight
            } else {
                null
            }
        }.getOrNull()
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
                        statusBarBackgroundType = resolveStatusBarBackgroundType(optionObject, componentsObject),
                        statusBarColor = resolveStatusBarColor(optionObject, componentsObject),
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
                            ringer = resolveAssetListOrSingle(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "ringer",
                                componentsObject = componentsObject,
                                key = "ringer",
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

    private fun resolveStatusBarBackgroundType(
        optionObject: JSONObject,
        componentsObject: JSONObject,
    ): String {
        val direct = optionObject.optString("statusBarBackgroundType")
        if (direct.isNotBlank()) return normalizeStatusBarBackgroundType(direct)
        val node = componentsObject.opt("statusBarBackground")
        val fromNode = when (node) {
            is JSONObject -> node.optString("type")
            else -> node?.toString().orEmpty()
        }
        if (fromNode.isNotBlank()) return normalizeStatusBarBackgroundType(fromNode)
        return "bluebackground"
    }

    private fun normalizeStatusBarBackgroundType(raw: String): String {
        return when (raw.trim().lowercase()) {
            "wallpaper" -> "wallpaper"
            "bluebackground", "blue_background", "blue-bg" -> "bluebackground"
            "color" -> "color"
            else -> "bluebackground"
        }
    }

    private fun resolveStatusBarColor(
        optionObject: JSONObject,
        componentsObject: JSONObject,
    ): String {
        val direct = optionObject.optString("statusBarColor")
        if (direct.isNotBlank()) return normalizeStatusBarColor(direct)
        val node = componentsObject.opt("statusBarBackground")
        val fromNode = if (node is JSONObject) node.optString("color") else ""
        if (fromNode.isNotBlank()) return normalizeStatusBarColor(fromNode)
        return ""
    }

    private fun normalizeStatusBarColor(raw: String): String {
        val normalized = raw.trim()
        if (normalized.isBlank()) return ""
        val withPrefix = if (normalized.startsWith("#")) normalized else "#$normalized"
        val hex = withPrefix.removePrefix("#")
        val isValid = (hex.length == 6 || hex.length == 8) && hex.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
        return if (isValid) "#${hex.uppercase()}" else ""
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
