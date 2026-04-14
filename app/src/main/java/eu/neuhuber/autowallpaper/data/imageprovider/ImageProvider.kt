package eu.neuhuber.autowallpaper.data.imageprovider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL

interface ImageProvider {
    suspend fun getImage(): Bitmap
}

class ImageProviderFactory {
    fun getProvider(name: String): ImageProvider {
        return when (name) {
            "Picsum" -> PicsumImageProvider
            "Bing" -> BingImageProvider
            else -> BingImageProvider
        }
    }
}

suspend fun fetchImage(url: URL): Bitmap = withContext(Dispatchers.IO) {
    try {
        val connection = url.openConnection()
        val response = connection.getInputStream()
        BitmapFactory.decodeStream(response)
            ?: throw Exception("Failed to decode image from $url")
    } catch (e: Exception) {
        Timber.e(e, "Error fetching image from $url")
        throw e
    }
}