package eu.neuhuber.autowallpaper.data.imageprovider

import android.graphics.Bitmap
import eu.neuhuber.autowallpaper.data.HttpClient
import kotlin.math.min

class BingImageProvider(private val httpClient: HttpClient) : ImageProvider {
    private val resolutions = listOf(
        "UHD",
        "1920x1200",
        "1920x1080",
        "1366x768",
        "1280x768",
        "1024x768",
        "800x600",
        "800x480",
        "1080x1920",
        "768x1280",
        "720x1280",
        "640x480",
        "480x800",
        "400x240",
        "320x240",
        "240x320"
    )

    override suspend fun getImage(width: Int, height: Int): Bitmap {
        val resolution = findBestResolution(width, height)
        val url =
            "https://wallpaper.oracle.neuhuber.eu/?resolution=$resolution&format=image&index=0&mkt=de-DE"
        return fetchImage(httpClient, url)
    }

    internal fun findBestResolution(targetWidth: Int, targetHeight: Int): String {
        val targetRatio = targetWidth.toDouble() / targetHeight
        val targetArea = targetWidth.toDouble() * targetHeight

        val parsedResolutions = resolutions.mapNotNull { res ->
            val (w, h) = when (res) {
                "UHD" -> 3840 to 2160
                else -> {
                    val parts = res.split("x")
                    if (parts.size == 2) {
                        val w = parts[0].toIntOrNull()
                        val h = parts[1].toIntOrNull()
                        if (w != null && h != null) w to h else return@mapNotNull null
                    } else return@mapNotNull null
                }
            }
            ResolutionInfo(w, h, res)
        }

        // Filter by the area limit to respect "max 2x" target.
        // We use a buffer (10%) to allow for close matches.
        val withinLimit = parsedResolutions.filter { it.width * it.height <= targetArea * 1.1 }

        val pool = if (withinLimit.isNotEmpty()) withinLimit else parsedResolutions

        // Pick the one that provides the most "effective" pixels after cropping to the target ratio.
        return pool.maxBy { res ->
            val croppedWidth = min(res.width.toDouble(), res.height * targetRatio)
            val croppedHeight = min(res.height.toDouble(), res.width / targetRatio)
            croppedWidth * croppedHeight
        }.name
    }

    private data class ResolutionInfo(val width: Int, val height: Int, val name: String)
}
