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

data class ThemeClockWidgetStrings(
    val default: String,
    val zh: String,
)

data class ThemeClockWidgetSupports(
    val styleSwitch: Boolean,
    val weather: Boolean,
    val steps: Boolean,
    val secondAnimation: Boolean,
)

data class ThemeClockWidgetAssets(
    val timeDigits: List<String>,
    val dateDigits: List<String>,
    val secondDigits: List<String>,
    val weekDays: List<String>,
    val secondDigitsNormalized: ThemeClockSecondDigitsNormalized?,
)

data class ThemeClockWidgetStyleVariable(
    val name: String,
    val default: Int,
    val min: Int,
    val max: Int,
    val selectionMode: String,
)

data class ThemeClockWidgetStyleVariables(
    val secondColorStyle: ThemeClockWidgetStyleVariable?,
)

data class ThemeClockWidgetRuntimeVariables(
    val secondTens: String,
    val secondOnes: String,
)

data class ThemeClockWidgetManifestLogic(
    val secondBasePath: String,
    val secondVariantSelector: String,
    val secondFrameGroupTens: String,
    val secondFrameGroupOnes: String,
)

data class ThemeClockStyleDefinition(
    val id: String,
    val label: String,
    val source: String,
    val timePrefix: String,
    val secondVariant: Int,
    val secondGlyphHint: String,
)

data class ThemeClockStyleCatalog13(
    val version: Int,
    val sourceFolders: List<String>,
    val styles: List<ThemeClockStyleDefinition>,
)

data class ThemeClockSecondRenderSlot(
    val variable: String,
    val min: Int,
    val max: Int,
)

data class ThemeClockSecondsRender(
    val tens: ThemeClockSecondRenderSlot?,
    val ones: ThemeClockSecondRenderSlot?,
)

data class ThemeClockSecondDigitsNormalized(
    val folder: String,
    val digitToGlyph: Map<String, String>,
    val glyphVariants: List<Int>,
    val filePattern: String,
    val secondsRender: ThemeClockSecondsRender?,
)

data class ThemeClockWidgetConfig(
    val type: String,
    val engine: String,
    val module: String,
    val assetRoot: String,
    val manifest: String,
    val strings: ThemeClockWidgetStrings,
    val size: String,
    val supports: ThemeClockWidgetSupports,
    val styleVariables: ThemeClockWidgetStyleVariables?,
    val runtimeVariables: ThemeClockWidgetRuntimeVariables?,
    val manifestLogic: ThemeClockWidgetManifestLogic?,
    val styleCatalog13: ThemeClockStyleCatalog13?,
    val assets: ThemeClockWidgetAssets,
)

data class ThemeBatteryWidgetVariables(
    val enable: String,
    val level: String,
    val state: String,
)

data class ThemeBatteryWidgetStateItem(
    val max: Int,
    val asset: String,
)

data class ThemeBatteryWidgetConfig(
    val type: String,
    val assetRoot: String,
    val driver: String,
    val description: String,
    val layout: String,
    val variables: ThemeBatteryWidgetVariables,
    val stateMap: Map<String, List<ThemeBatteryWidgetStateItem>>,
    val statusbarSprite: ThemeBatterySpriteSheet?,
)

data class ThemeOptionComponents(
    val wallpaper: ThemeWallpaperConfig,
    val lockScreen: String,
    val clockWidget: ThemeClockWidgetConfig?,
    val batteryWidget: ThemeBatteryWidgetConfig?,
    val battery: ThemeBatterySpriteSheet?,
    val wifi: List<String>,
    val signal: String,
    val bluetooth: List<String>,
    val charge: String,
    val data: List<String>,
    val hotspot: String,
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
        ?: batteryWidget?.statusbarSprite?.modes?.get("normal")?.asset
        ?: batteryWidget?.statusbarSprite?.modes?.values?.firstOrNull()?.asset
        ?: charge

object ThemeOptionCatalog {
    private const val ROOT_ASSET_DIR = "theme_options"
    private val PACK_DIRS = listOf("xiyxiy", "xiyxiy2")
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
                    val batteryWidget = parseBatteryWidgetConfig(assets, packDir, componentsObject)

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
                            clockWidget = parseClockWidgetConfig(
                                assets = assets,
                                packDir = packDir,
                                componentsObject = componentsObject,
                            ),
                            batteryWidget = batteryWidget,
                            battery = parseBatteryConfig(
                                assets = assets,
                                packDir = packDir,
                                componentsObject = componentsObject,
                            ) ?: batteryWidget?.statusbarSprite,
                            wifi = resolveAssetArray(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "wifi",
                                componentsObject = componentsObject,
                                key = "wifi",
                            ),
                            signal = resolveAssetPath(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "signal",
                                assetKey = componentsObject.optString("signal"),
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
                            hotspot = resolveAssetPath(
                                assets = assets,
                                packDir = packDir,
                                componentDir = "hotspot",
                                assetKey = componentsObject.optString("hotspot"),
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

    private fun parseClockWidgetConfig(
        assets: AssetManager,
        packDir: String,
        componentsObject: JSONObject,
    ): ThemeClockWidgetConfig? {
        val clockObject = componentsObject.optJSONObject("clock_widget") ?: return null
        val assetRoot = normalizeAssetRoot(
            parentAssetRoot = "",
            rawAssetRoot = clockObject.optString("assetRoot"),
            fallback = "assets/widgets/clock_2x4",
        )

        val stringsObject = clockObject.optJSONObject("strings") ?: JSONObject()
        val strings = ThemeClockWidgetStrings(
            default = resolveAssetPathByRoot(
                assets = assets,
                packDir = packDir,
                assetRoot = assetRoot,
                assetKey = stringsObject.optString("default"),
            ),
            zh = resolveAssetPathByRoot(
                assets = assets,
                packDir = packDir,
                assetRoot = assetRoot,
                assetKey = stringsObject.optString("zh"),
            ),
        )

        val supportsObject = clockObject.optJSONObject("supports") ?: JSONObject()
        val supports = ThemeClockWidgetSupports(
            styleSwitch = supportsObject.optBoolean("styleSwitch"),
            weather = supportsObject.optBoolean("weather"),
            steps = supportsObject.optBoolean("steps"),
            secondAnimation = supportsObject.optBoolean("secondAnimation"),
        )

        val styleVariablesObject = clockObject.optJSONObject("styleVariables")
        val styleVariables = styleVariablesObject?.let { styleJson ->
            val secondColorStyleObject = styleJson.optJSONObject("secondColorStyle")
            ThemeClockWidgetStyleVariables(
                secondColorStyle = secondColorStyleObject?.let { secondStyle ->
                    ThemeClockWidgetStyleVariable(
                        name = secondStyle.optString("name"),
                        default = secondStyle.optInt("default"),
                        min = secondStyle.optInt("min"),
                        max = secondStyle.optInt("max"),
                        selectionMode = secondStyle.optString("selectionMode"),
                    )
                },
            )
        }

        val runtimeVariablesObject = clockObject.optJSONObject("runtimeVariables")
        val runtimeVariables = runtimeVariablesObject?.let {
            ThemeClockWidgetRuntimeVariables(
                secondTens = it.optString("secondTens"),
                secondOnes = it.optString("secondOnes"),
            )
        }

        val manifestLogicObject = clockObject.optJSONObject("manifestLogic")
        val manifestLogic = manifestLogicObject?.let {
            ThemeClockWidgetManifestLogic(
                secondBasePath = it.optString("secondBasePath"),
                secondVariantSelector = it.optString("secondVariantSelector"),
                secondFrameGroupTens = it.optString("secondFrameGroupTens"),
                secondFrameGroupOnes = it.optString("secondFrameGroupOnes"),
            )
        }

        val styleCatalog13Object = clockObject.optJSONObject("styleCatalog13")
        val styleCatalog13 = styleCatalog13Object?.let { catalog ->
            val foldersArray = catalog.optJSONArray("sourceFolders") ?: JSONArray()
            val sourceFolders = buildList {
                for (index in 0 until foldersArray.length()) {
                    val folder = foldersArray.optString(index)
                    if (folder.isNotBlank()) add(folder)
                }
            }

            val stylesArray = catalog.optJSONArray("styles") ?: JSONArray()
            val styles = buildList {
                for (index in 0 until stylesArray.length()) {
                    val styleObject = stylesArray.optJSONObject(index) ?: continue
                    add(
                        ThemeClockStyleDefinition(
                            id = styleObject.optString("id").ifBlank { "style_${index + 1}" },
                            label = styleObject.optString("label").ifBlank { "Style ${index + 1}" },
                            source = styleObject.optString("source"),
                            timePrefix = resolveRelativePathByRoot(
                                packDir = packDir,
                                assetRoot = assetRoot,
                                relativePath = styleObject.optString("timePrefix"),
                            ),
                            secondVariant = styleObject.optInt("secondVariant"),
                            secondGlyphHint = styleObject.optString("secondGlyphHint"),
                        ),
                    )
                }
            }

            ThemeClockStyleCatalog13(
                version = catalog.optInt("version", 1),
                sourceFolders = sourceFolders,
                styles = styles,
            )
        }

        val assetsObject = clockObject.optJSONObject("assets") ?: JSONObject()
        val secondDigitsNormalizedObject = assetsObject.optJSONObject("secondDigitsNormalized")
        val secondDigitsNormalized = secondDigitsNormalizedObject?.let { normalized ->
            val digitToGlyphObject = normalized.optJSONObject("digitToGlyph") ?: JSONObject()
            val digitToGlyph = linkedMapOf<String, String>()
            val glyphKeys = digitToGlyphObject.keys()
            while (glyphKeys.hasNext()) {
                val key = glyphKeys.next()
                digitToGlyph[key] = digitToGlyphObject.optString(key)
            }

            val glyphVariantsArray = normalized.optJSONArray("glyphVariants") ?: JSONArray()
            val glyphVariants = buildList {
                for (index in 0 until glyphVariantsArray.length()) {
                    add(glyphVariantsArray.optInt(index))
                }
            }

            val secondsRenderObject = normalized.optJSONObject("secondsRender")
            val secondsRender = secondsRenderObject?.let { render ->
                ThemeClockSecondsRender(
                    tens = render.optJSONObject("tens")?.let { tens ->
                        val range = tens.optJSONArray("validRange") ?: JSONArray()
                        ThemeClockSecondRenderSlot(
                            variable = tens.optString("var"),
                            min = range.optInt(0),
                            max = range.optInt(1),
                        )
                    },
                    ones = render.optJSONObject("ones")?.let { ones ->
                        val range = ones.optJSONArray("validRange") ?: JSONArray()
                        ThemeClockSecondRenderSlot(
                            variable = ones.optString("var"),
                            min = range.optInt(0),
                            max = range.optInt(1),
                        )
                    },
                )
            }

            ThemeClockSecondDigitsNormalized(
                folder = resolveRelativePathByRoot(
                    packDir = packDir,
                    assetRoot = assetRoot,
                    relativePath = normalized.optString("folder"),
                ),
                digitToGlyph = digitToGlyph,
                glyphVariants = glyphVariants,
                filePattern = normalized.optString("filePattern"),
                secondsRender = secondsRender,
            )
        }

        val widgetAssets = ThemeClockWidgetAssets(
            timeDigits = resolveRelativeAssetArrayByRoot(
                packDir = packDir,
                assetRoot = assetRoot,
                array = assetsObject.optJSONArray("timeDigits"),
            ),
            dateDigits = resolveRelativeAssetArrayByRoot(
                packDir = packDir,
                assetRoot = assetRoot,
                array = assetsObject.optJSONArray("dateDigits"),
            ),
            secondDigits = resolveRelativeAssetArrayByRoot(
                packDir = packDir,
                assetRoot = assetRoot,
                array = assetsObject.optJSONArray("secondDigits"),
            ),
            weekDays = resolveRelativeAssetArrayByRoot(
                packDir = packDir,
                assetRoot = assetRoot,
                array = assetsObject.optJSONArray("weekDays"),
            ),
            secondDigitsNormalized = secondDigitsNormalized,
        )

        return ThemeClockWidgetConfig(
            type = clockObject.optString("type"),
            engine = clockObject.optString("engine"),
            module = clockObject.optString("module"),
            assetRoot = buildAssetRootPath(packDir, assetRoot),
            manifest = resolveAssetPathByRoot(
                assets = assets,
                packDir = packDir,
                assetRoot = assetRoot,
                assetKey = clockObject.optString("manifest"),
            ),
            strings = strings,
            size = clockObject.optString("size"),
            supports = supports,
            styleVariables = styleVariables,
            runtimeVariables = runtimeVariables,
            manifestLogic = manifestLogic,
            styleCatalog13 = styleCatalog13,
            assets = widgetAssets,
        )
    }

    private fun parseBatteryWidgetConfig(
        assets: AssetManager,
        packDir: String,
        componentsObject: JSONObject,
    ): ThemeBatteryWidgetConfig? {
        val batteryWidgetObject = componentsObject.optJSONObject("battery_widget") ?: return null
        val assetRoot = normalizeAssetRoot(
            parentAssetRoot = "",
            rawAssetRoot = batteryWidgetObject.optString("assetRoot"),
            fallback = "assets/widgets/battery_widget",
        )

        val variablesObject = batteryWidgetObject.optJSONObject("variables") ?: JSONObject()
        val variables = ThemeBatteryWidgetVariables(
            enable = variablesObject.optString("enable"),
            level = variablesObject.optString("level"),
            state = variablesObject.optString("state"),
        )

        val stateMapObject = batteryWidgetObject.optJSONObject("stateMap") ?: JSONObject()
        val stateMap = linkedMapOf<String, List<ThemeBatteryWidgetStateItem>>()
        val states = stateMapObject.keys()
        while (states.hasNext()) {
            val stateName = states.next()
            val itemsArray = stateMapObject.optJSONArray(stateName) ?: JSONArray()
            val mappedItems = mutableListOf<ThemeBatteryWidgetStateItem>()
            for (index in 0 until itemsArray.length()) {
                val itemObject = itemsArray.optJSONObject(index) ?: continue
                mappedItems += ThemeBatteryWidgetStateItem(
                    max = itemObject.optInt("max"),
                    asset = resolveAssetPathByRoot(
                        assets = assets,
                        packDir = packDir,
                        assetRoot = assetRoot,
                        assetKey = itemObject.optString("asset"),
                    ),
                )
            }
            stateMap[stateName] = mappedItems
        }

        val statusbarSprite = batteryWidgetObject.optJSONObject("statusbarSprite")?.let {
            parseBatterySpriteSheet(
                assets = assets,
                packDir = packDir,
                spriteObject = it,
                parentAssetRoot = assetRoot,
                fallbackAssetRoot = "$assetRoot/statusbar",
            )
        }

        return ThemeBatteryWidgetConfig(
            type = batteryWidgetObject.optString("type"),
            assetRoot = buildAssetRootPath(packDir, assetRoot),
            driver = batteryWidgetObject.optString("driver"),
            description = resolveAssetPathByRoot(
                assets = assets,
                packDir = packDir,
                assetRoot = assetRoot,
                assetKey = batteryWidgetObject.optString("description"),
            ),
            layout = resolveAssetPathByRoot(
                assets = assets,
                packDir = packDir,
                assetRoot = assetRoot,
                assetKey = batteryWidgetObject.optString("layout"),
            ),
            variables = variables,
            stateMap = stateMap,
            statusbarSprite = statusbarSprite,
        )
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

    private fun resolveRelativeAssetArrayByRoot(
        packDir: String,
        assetRoot: String,
        array: JSONArray?,
    ): List<String> {
        if (array == null) return emptyList()
        val resolved = mutableListOf<String>()
        for (index in 0 until array.length()) {
            val relative = array.optString(index)
            if (relative.isBlank()) continue
            resolved += resolveRelativePathByRoot(packDir, assetRoot, relative)
        }
        return resolved
    }

    private fun resolveRelativePathByRoot(
        packDir: String,
        assetRoot: String,
        relativePath: String,
    ): String {
        if (relativePath.isBlank()) return ""
        val rootPath = buildAssetRootPath(packDir, assetRoot)
        return "$rootPath/$relativePath".replace("//", "/")
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
