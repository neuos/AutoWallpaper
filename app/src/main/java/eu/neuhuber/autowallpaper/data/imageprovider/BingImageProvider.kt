package eu.neuhuber.autowallpaper.data.imageprovider

import android.graphics.Bitmap
import android.util.Xml
import eu.neuhuber.autowallpaper.data.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber


class BingImageProvider(private val httpClient: HttpClient) : ImageProvider {
    companion object {
        private val resolutions = setOf(
            ResolutionInfo(Dimension(3840, 2160), "UHD"),
            ResolutionInfo(Dimension(1920, 1200), "1920x1200"),
            ResolutionInfo(Dimension(1920, 1080), "1920x1080"),
            ResolutionInfo(Dimension(1366, 768), "1366x768"),
            ResolutionInfo(Dimension(1280, 768), "1280x768"),
            ResolutionInfo(Dimension(1024, 768), "1024x768"),
            ResolutionInfo(Dimension(800, 600), "800x600"),
            ResolutionInfo(Dimension(800, 480), "800x480"),
            ResolutionInfo(Dimension(1080, 1920), "1080x1920"),
            ResolutionInfo(Dimension(768, 1280), "768x1280"),
            ResolutionInfo(Dimension(720, 1280), "720x1280"),
            ResolutionInfo(Dimension(640, 480), "640x480"),
            ResolutionInfo(Dimension(480, 800), "480x800"),
            ResolutionInfo(Dimension(400, 240), "400x240"),
            ResolutionInfo(Dimension(320, 240), "320x240"),
            ResolutionInfo(Dimension(240, 320), "240x320"),
        )
    }

    private val resolutionCache = mutableMapOf<Dimension, String>()

    override suspend fun getImage(dimension: Dimension): Bitmap {
        val resolution = resolutionCache.getOrPut(dimension) {
            resolutions.getBestResolution(dimension)
        }
        val urlPath = fetchUrlPath()
        val urlBase = "https://www.bing.com"
        val url = "$urlBase${urlPath}_$resolution.jpg"
        return fetchImage(httpClient, url)
    }

    private suspend fun fetchUrlPath(): String = withContext(Dispatchers.IO) {
        val bingApiUrl = "https://www.bing.com/HPImageArchive.aspx?format=xml&idx=0&n=1&mkt=de-DE"
        Timber.d("Fetching Bing wallpaper metadata from $bingApiUrl")
        try {
            httpClient.getStream(bingApiUrl).use { inputStream ->
                val parser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(inputStream, null)
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "urlBase") {
                        return@withContext parser.nextText()
                    }
                    eventType = parser.next()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching Bing wallpaper metadata")
            throw e
        }
        throw Exception("Failed to find urlBase in Bing API response")
    }
}

