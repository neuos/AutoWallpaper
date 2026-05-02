package eu.neuhuber.autowallpaper.data.imageprovider

import kotlin.math.min

data class Dimension(val width: Int, val height: Int) {
    val ratio: Double = width.toDouble() / height
    val area: Int = width * height

    override fun toString() = "$width x $height"
}

data class ResolutionInfo(val dimension: Dimension, val name: String)

fun Set<ResolutionInfo>.getBestResolution(target: Dimension): String {
    require(isNotEmpty(), { "No resolutions provided to find best for target $target" })
    return maxByOrNull { res ->
        val ratioDiff = kotlin.math.abs(res.dimension.ratio - target.ratio)
        val effectiveArea = res.dimension.withRatio(target.ratio).area.toDouble()
        // High penalty for ratio mismatch ensures we favor "hand-picked" crops.
        effectiveArea / (1 + ratioDiff * 50)
    }?.name ?: first().name
}

/**
 * Ensures that width and height lie in the specified range.
 * Keeps the aspect ratio
 */
fun Dimension.coerceIn(min: Int, max: Int): Dimension {
    return Dimension(width.coerceIn(min, max), height.coerceIn(min, max)).withRatio(ratio)
}

/**
 * Returns the maximum size dimension with the given aspect ratio that fits inside the given width and height.
 */
fun Dimension.withRatio(
    ratio: Double
): Dimension {
    val width = min(width, (height * ratio).toInt())
    val height = min(height, (width / ratio).toInt())
    return Dimension(width, height)
}
