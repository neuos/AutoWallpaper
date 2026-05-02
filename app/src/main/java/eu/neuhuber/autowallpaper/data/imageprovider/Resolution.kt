package eu.neuhuber.autowallpaper.data.imageprovider

import timber.log.Timber
import kotlin.math.abs
import kotlin.math.min

data class Dimension(val width: Int, val height: Int) {
    val ratio: Double = width.toDouble() / height
    val area: Int = width * height

    override fun toString() = "${width}x${height}"
}

data class ResolutionInfo(val dimension: Dimension, val name: String)

fun Set<ResolutionInfo>.getBestResolution(target: Dimension): String {
    require(isNotEmpty()) { "No resolutions provided to find best for target $target" }
    Timber.d("finding best resolution for $target")

    val largeEnough = filter { it.dimension.withRatio(target.ratio).area >= target.area }

    val best = if (largeEnough.isNotEmpty()) {
        Timber.d("Found ${largeEnough.size} resolutions large enough")
        largeEnough.minWith(
            compareBy<ResolutionInfo> { abs(it.dimension.ratio - target.ratio) }
                .thenByDescending { it.dimension.area }
        )
    } else {
        Timber.d("No resolution large enough, using heuristic")
        maxBy { res ->
            val ratioDiff = abs(res.dimension.ratio - target.ratio)
            val effectiveArea = res.dimension.withRatio(target.ratio).area.toDouble()
            effectiveArea / (1 + ratioDiff * 10)
        }
    }

    return best.name.also {
        Timber.d("best resolution found is $best")
    }
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
