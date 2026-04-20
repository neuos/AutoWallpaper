package eu.neuhuber.autowallpaper.data.imageprovider

import eu.neuhuber.autowallpaper.data.HttpClient
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class PicsumImageProviderTest {

    @Test
    fun `getImage should use same seed until reset`() = runBlocking {
        val httpClient = mockk<HttpClient>()
        val urlSlot = mutableListOf<String>()

        coEvery { httpClient.getStream(capture(urlSlot)) } returns ByteArrayInputStream(ByteArray(0))

        val provider = PicsumImageProvider(httpClient)

        runCatching {
            provider.getImage(100, 200)
        }
        runCatching {
            provider.getImage(300, 400)
        }

        val seed1 = extractSeed(urlSlot[0])
        val seed2 = extractSeed(urlSlot[1])

        assertEquals("Seed should be the same for consecutive calls", seed1, seed2)

        provider.resetSeed()
        runCatching {
            provider.getImage(500, 600)
        }

        val seed3 = extractSeed(urlSlot[2])
        assertNotEquals("Seed should change after reset", seed1, seed3)
    }

    private fun extractSeed(url: String): String {
        // https://picsum.photos/seed/SEED/width/height
        return url.split("/")[4]
    }
}
