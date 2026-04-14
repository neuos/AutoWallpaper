package eu.neuhuber.autowallpaper.data.imageprovider

import android.graphics.Bitmap
import java.net.URL

object BingImageProvider : ImageProvider {
    private val url =
        URL("https://wallpaper.oracle.neuhuber.eu/?resolution=UHD&format=image&index=0&mkt=de-DE")

    override suspend fun getImage(): Bitmap = fetchImage(url)
}
