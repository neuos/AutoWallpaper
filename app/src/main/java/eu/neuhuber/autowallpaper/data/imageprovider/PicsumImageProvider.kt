package eu.neuhuber.autowallpaper.data.imageprovider

import android.graphics.Bitmap
import eu.neuhuber.autowallpaper.data.HttpClient
import java.util.UUID

class PicsumImageProvider(private val httpClient: HttpClient) : ImageProvider {
    private var seed: String = UUID.randomUUID().toString()

    override suspend fun getImage(dimension: Dimension): Bitmap {
        // Picsum has a maximum dimension of 5000px
        val capped = dimension.coerceIn(1, 5000)
        val url = "https://picsum.photos/seed/$seed/${capped.width}/${capped.height}"
        return fetchImage(httpClient, url)
    }

    fun resetSeed() {
        seed = UUID.randomUUID().toString()
    }
}
