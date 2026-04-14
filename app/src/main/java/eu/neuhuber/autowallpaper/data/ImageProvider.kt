package eu.neuhuber.autowallpaper.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL

interface ImageProvider {
    suspend fun getImage(): Bitmap
}

object BingImageProvider : ImageProvider {
    val url = URL("https://wallpaper.oracle.neuhuber.eu/?resolution=UHD&format=image&index=0&mkt=de-DE")

    override suspend fun getImage(): Bitmap {
        return withContext(Dispatchers.IO) {
            try {
                val connection = url.openConnection()
                val response = connection.getInputStream()
                BitmapFactory.decodeStream(response) ?: throw Exception("Failed to decode image")
            } catch (e: Exception) {
                Timber.e(e, "Error fetching image from Bing")
                throw e
            }
        }
    }
}
