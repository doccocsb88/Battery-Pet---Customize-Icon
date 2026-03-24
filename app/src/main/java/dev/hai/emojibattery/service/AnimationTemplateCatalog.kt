package dev.hai.emojibattery.service

data class AnimationTemplate(
    val id: Int,
    val assetPath: String,
) {
    val isLottie: Boolean
        get() = assetPath.endsWith(".json", ignoreCase = true)
}

/**
 * Mirrors the original app's local animation catalog (C5914jz0.a()).
 */
object AnimationTemplateCatalog {
    val templates: List<AnimationTemplate> = listOf(
        AnimationTemplate(0, "cute_animation/1.gif"),
        AnimationTemplate(1, "cute_animation/2.gif"),
        AnimationTemplate(2, "cute_animation/3.gif"),
        AnimationTemplate(3, "cute_animation/4.gif"),
        AnimationTemplate(4, "cute_animation/5.gif"),
        AnimationTemplate(5, "cute_animation/6.gif"),
        AnimationTemplate(6, "cute_animation/7.gif"),
        AnimationTemplate(7, "cute_animation/8.gif"),
        AnimationTemplate(8, "cute_animation/9.gif"),
        AnimationTemplate(9, "cute_animation/10.gif"),
        AnimationTemplate(10, "cute_animation/11.gif"),
        AnimationTemplate(11, "cute_animation/12.gif"),
        AnimationTemplate(12, "cute_animation/13.gif"),
        AnimationTemplate(13, "cute_animation/14.gif"),
        AnimationTemplate(14, "cute_animation/15.gif"),
        AnimationTemplate(15, "cute_animation/16.gif"),
        AnimationTemplate(16, "cute_animation/17.gif"),
        AnimationTemplate(17, "cute_animation/18.gif"),
        AnimationTemplate(18, "cute_animation/19.gif"),
        AnimationTemplate(19, "cute_animation/20.gif"),
        AnimationTemplate(20, "cute_animation/cute_gif.gif"),
        AnimationTemplate(21, "cute_animation/cute_2.json"),
        AnimationTemplate(22, "cute_animation/cute_3.json"),
        AnimationTemplate(23, "cute_animation/cute_4.json"),
        AnimationTemplate(24, "cute_animation/cute_5.json"),
    )

    fun resolve(id: Int): AnimationTemplate = templates.firstOrNull { it.id == id } ?: templates.first()
}
