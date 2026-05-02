package eu.neuhuber.autowallpaper.data.imageprovider

import kotlin.math.min

data class Dimension(val width: Int, val height: Int) {
    val ratio: Double = width.toDouble() / height
    val area: Int = width * height

    companion object
}

data class ResolutionInfo(val dimension: Dimension, val name: String)

fun Set<ResolutionInfo>.getBestResolution(target: Dimension): String =
    filter { it.dimension.width * it.dimension.height <= target.area * 2 }.ifEmpty { this }
        .maxBy { res ->
            res.dimension.withRatio(target.ratio).area // Pick the one that provides the most "effective" pixels after cropping to the target ratio.
        }.name

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
