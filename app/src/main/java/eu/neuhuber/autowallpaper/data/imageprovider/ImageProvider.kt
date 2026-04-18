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

suspend fun fetchImage(url: URL): Bitmap = withContext(Dispatchers.IO) {
    try {
        url.openConnection().apply {
            connectTimeout = 10_000 // 10 seconds
            readTimeout = 10_000
        }.getInputStream().use { response ->
            BitmapFactory.decodeStream(response)
                ?: throw Exception("Failed to decode image from $url")
        }
    } catch (e: Exception) {
        Timber.e(e, "Error fetching image from $url")
        throw e
    }
}