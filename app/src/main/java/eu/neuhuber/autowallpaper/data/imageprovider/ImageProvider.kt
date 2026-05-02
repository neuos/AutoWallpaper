package eu.neuhuber.autowallpaper.data.imageprovider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import eu.neuhuber.autowallpaper.data.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

interface ImageProvider {
    suspend fun getImage(dimension: Dimension): Bitmap
}

suspend fun fetchImage(httpClient: HttpClient, url: String): Bitmap = withContext(Dispatchers.IO) {
    Timber.d("Starting image download from provider URL: $url")
    try {
        httpClient.getStream(url).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
                ?: throw Exception("Failed to decode bitmap from $url")
        }
    } catch (e: Exception) {
        Timber.e(e, "Error fetching image from $url")
        throw e
    }
}
