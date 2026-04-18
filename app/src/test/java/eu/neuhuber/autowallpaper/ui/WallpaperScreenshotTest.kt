package eu.neuhuber.autowallpaper.ui

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import eu.neuhuber.autowallpaper.model.WallpaperSettings
import eu.neuhuber.autowallpaper.ui.theme.AutoWallpaperTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w411dp-h891dp-xhdpi") // Taller device (e.g., Pixel 7 Pro)
class WallpaperScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        stopKoin()
        startKoin {
            modules()
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun snapshotSettingsContent() {
        composeTestRule.setContent {
            AutoWallpaperTheme {
                SettingsContent(
                    settings = WallpaperSettings(),
                    isLoading = false,
                    onSettingsChange = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun snapshotMainScreen() {
        val imageFile = File("src/test/resources/test_wallpaper.jpg")
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val imageBitmap = bitmap.asImageBitmap()

        val state = WallpaperUiState(
            bitmap = imageBitmap,
            settings = WallpaperSettings(),
            isUpdateInProgress = false
        )

        composeTestRule.setContent {
            AutoWallpaperTheme(imageBitmap = imageBitmap) {
                MainScreen(
                    state = state,
                    onDownload = {},
                    onApply = {},
                    onUpdateSettings = {},
                    initialShowSettings = false
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun snapshotMainScreenWithSettings() {
        val imageFile = File("src/test/resources/test_wallpaper.jpg")
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val imageBitmap = bitmap.asImageBitmap()

        val state = WallpaperUiState(
            bitmap = imageBitmap,
            settings = WallpaperSettings(),
            isUpdateInProgress = false
        )

        composeTestRule.setContent {
            AutoWallpaperTheme(imageBitmap = imageBitmap) {
                MainScreen(
                    state = state,
                    onDownload = {},
                    onApply = {},
                    onUpdateSettings = {},
                    initialShowSettings = true
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
