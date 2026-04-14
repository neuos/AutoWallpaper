package eu.neuhuber.autowallpaper.data.imageprovider

import android.graphics.Bitmap
import java.net.URL

object PicsumImageProvider : ImageProvider {
    private val url = URL("https://picsum.photos/3840/2160")

    override suspend fun getImage(): Bitmap = fetchImage(url)
}
