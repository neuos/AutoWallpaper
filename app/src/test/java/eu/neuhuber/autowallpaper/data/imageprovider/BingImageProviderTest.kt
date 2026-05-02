package eu.neuhuber.autowallpaper.data.imageprovider

import android.graphics.BitmapFactory
import eu.neuhuber.autowallpaper.data.HttpClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class BingImageProviderTest {

    @Test
    fun `getImage should parse urlBase and append resolution`() = runBlocking {
        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeStream(any()) } returns mockk()

        val httpClient = mockk<HttpClient>()
        val capturedUrls = mutableListOf<String>()

        val xmlResponse = """
            <images>
                <image>
                    <urlBase>/th?id=OHR.TestImage</urlBase>
                </image>
            </images>
        """.trimIndent()

        coEvery { httpClient.getStream(capture(capturedUrls)) } returnsMany listOf(
            ByteArrayInputStream(xmlResponse.toByteArray()),
            ByteArrayInputStream(ByteArray(0))
        )

        val provider = BingImageProvider(httpClient)
        val target = Dimension(1080, 1920)

        provider.getImage(target)

        assertTrue(
            "First call should be to Bing API",
            capturedUrls[0].contains("HPImageArchive.aspx")
        )
        assertTrue(
            "Second call should be for the image with selected resolution",
            capturedUrls[1].startsWith("https://www.bing.com/th?id=OHR.TestImage_")
        )
        assertTrue("URL should end with .jpg", capturedUrls[1].endsWith(".jpg"))
    }
}
