package eu.neuhuber.autowallpaper.ui

import androidx.work.WorkManager
import eu.neuhuber.autowallpaper.data.SettingsRepository
import eu.neuhuber.autowallpaper.data.imageprovider.ImageProvider
import eu.neuhuber.autowallpaper.model.WallpaperSettings
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest

@OptIn(ExperimentalCoroutinesApi::class)
class WallpaperViewModelTest : KoinTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: SettingsRepository
    private lateinit var workManager: WorkManager
    private lateinit var mockBingProvider: ImageProvider
    private lateinit var viewModel: WallpaperViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        repository = mockk(relaxed = true)
        workManager = mockk(relaxed = true)
        mockBingProvider = mockk()

        every { repository.settingsFlow } returns flowOf(WallpaperSettings(provider = "Bing"))

        startKoin {
            modules(module {
                single(named("Bing")) { mockBingProvider }
            })
        }

        viewModel = WallpaperViewModel(repository, workManager)
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default settings`() = runTest {
        advanceUntilIdle()
        assertEquals("Bing", viewModel.state.settings.provider)
    }
}
