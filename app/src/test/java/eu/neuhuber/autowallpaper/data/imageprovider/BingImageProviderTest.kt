package eu.neuhuber.autowallpaper.data.imageprovider

import eu.neuhuber.autowallpaper.data.HttpClient
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.ByteArrayInputStream

@RunWith(Parameterized::class)
class BingImageProviderTest(
    private val targetWidth: Int,
    private val targetHeight: Int,
    private val expectedResolutionPatterns: List<String>,
    private val description: String
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{3} ({0}x{1})")
        fun data() = listOf(
            // Modern Portrait (1080x2400) -> 2x is 2160x4800. Area = 10.3MP. Capped to 1800x4000. Area = 7.2MP.
            // UHD (8.3MP) is excluded by 7.2MP * 1.1 = 7.92 limit.
            // 1080x1920 (2.07MP) included.
            arrayOf(1800, 4000, listOf("1080x1920"), "Modern Portrait Phone"),

            // Scrolling (3*1080 x 2400) -> 2x is 6480x4800. Capped to 4000x2962. Area = 11.8MP.
            // UHD (8.3MP) included. Target Ratio 1.35.
            arrayOf(4000, 2962, listOf("UHD"), "Modern Scrolling Wallpaper"),

            // 4K Monitor -> 3840x2160. Area = 8.3MP.
            arrayOf(3840, 2160, listOf("UHD"), "4K Landscape"),

            // Old phone (480x800) -> 2x is 960x1600. Area = 1.53MP.
            // 768x1280 (0.98MP) included.
            // 1080x1920 (2.07MP) excluded.
            arrayOf(960, 1600, listOf("768x1280"), "Old Phone Portrait"),

            // Square-ish display (e.g. 500x500) -> 2x is 1000x1000. Area = 1MP.
            // 1366x768 (1.05MP) included. Target Ratio 1.0. Effective Area 768*768 = 0.59MP.
            arrayOf(1000, 1000, listOf("1366x768"), "Small Square Display")
        )
    }

    @Test
    fun `should pick best resolution for various screen sizes`() = runBlocking {
        val httpClient = mockk<HttpClient>()
        val urlSlot = slot<String>()

        coEvery { httpClient.getStream(capture(urlSlot)) } returns ByteArrayInputStream(ByteArray(0))

        val provider = BingImageProvider(httpClient)

        runCatching {
            provider.getImage(targetWidth, targetHeight)
        }

        val capturedUrl = urlSlot.captured
        val match =
            expectedResolutionPatterns.any { pattern -> capturedUrl.contains("resolution=$pattern") }

        assertTrue(
            "For $description ($targetWidth x $targetHeight), expected one of $expectedResolutionPatterns but got $capturedUrl",
            match
        )
    }
}
