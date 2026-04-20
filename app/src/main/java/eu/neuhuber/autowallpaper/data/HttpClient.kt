package eu.neuhuber.autowallpaper.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

interface HttpClient {
    suspend fun getStream(url: String): InputStream
}

class DefaultHttpClient : HttpClient {
    override suspend fun getStream(url: String): InputStream = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        val responseCode = connection.responseCode
        if (responseCode in 200..299) {
            connection.inputStream
        } else {
            throw Exception("HTTP error $responseCode for $url")
        }
    }
}
