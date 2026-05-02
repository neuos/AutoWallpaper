package eu.neuhuber.autowallpaper.data.imageprovider

import org.junit.Assert.assertEquals
import org.junit.Test

class ResolutionTest {
    private val resolutions = setOf(
        ResolutionInfo(Dimension(3840, 2160), "UHD"),        // 1.77 ratio
        ResolutionInfo(Dimension(1920, 1080), "1920x1080"),  // 1.77 ratio
        ResolutionInfo(Dimension(1080, 1920), "1080x1920"),  // 0.56 ratio
        ResolutionInfo(Dimension(768, 1280), "768x1280"),    // 0.6 ratio
        ResolutionInfo(Dimension(1366, 768), "1366x768"),    // 1.77 ratio
    )

    @Test
    fun `should prefer portrait resolution for portrait phone`() {
        val target = Dimension(1080, 2400) // 0.45 ratio
        // Even though UHD is much larger, 1080x1920 is a better aspect ratio match
        val best = resolutions.getBestResolution(target)
        assertEquals("1080x1920", best)
    }

    @Test
    fun `should prefer UHD for 4K monitor`() {
        val target = Dimension(3840, 2160) // 1.77 ratio
        val best = resolutions.getBestResolution(target)
        assertEquals("UHD", best)
    }

    @Test
    fun `should pick highest available resolution when ratios match`() {
        val target = Dimension(1600, 900) // 1.77 ratio
        val best = resolutions.getBestResolution(target)
        assertEquals("UHD", best)
    }

    @Test
    fun `should prefer better ratio even if slightly lower resolution`() {
        // Target ratio 0.6
        val target = Dimension(600, 1000)
        // 768x1280 (ratio 0.6) is a perfect match.
        // 1080x1920 (ratio 0.56) is higher res but worse ratio.
        val best = resolutions.getBestResolution(target)
        assertEquals("768x1280", best)
    }

    @Test
    fun `should not select a pixelated resolution`() {
        // Even if a low-res image has a slightly better ratio, we should prefer the high-res one
        // to avoid pixelation on high-res screens.
        val target = Dimension(4000, 2963) // ~1.35 ratio
        val localResolutions =
            resolutions + ResolutionInfo(Dimension(1024, 768), "1024x768") // 1.33 ratio
        val best = localResolutions.getBestResolution(target)
        assertEquals("UHD", best)
    }

    @Test
    fun `should fallback to first if set is empty`() {
        val single = setOf(ResolutionInfo(Dimension(100, 100), "MINI"))
        assertEquals("MINI", single.getBestResolution(Dimension(1000, 1000)))
    }
}
