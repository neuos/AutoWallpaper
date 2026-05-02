package eu.neuhuber.autowallpaper

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import eu.neuhuber.autowallpaper.ui.MainScreen
import eu.neuhuber.autowallpaper.ui.WallpaperUiEvent
import eu.neuhuber.autowallpaper.ui.WallpaperViewModel
import eu.neuhuber.autowallpaper.ui.theme.AutoWallpaperTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNormalMode()
    }

    private fun handleNormalMode() {
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val viewModel: WallpaperViewModel = koinViewModel()
            val state = viewModel.state

            LaunchedEffect(key1 = true) {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.events.collect { event ->
                        when (event) {
                            is WallpaperUiEvent.Success -> {
                                Toast.makeText(context, "Wallpaper set!", Toast.LENGTH_SHORT).show()
                            }
                            is WallpaperUiEvent.Error -> {
                                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }

            AutoWallpaperTheme(seedColor = state.seedColor) {
                MainScreen(
                    state = state,
                    onDownload = { viewModel.downloadImage() },
                    onApply = { viewModel.applyWallpaper() },
                    onUpdateSettings = { viewModel.updateSettings(it) }
                )
            }
        }
    }
}
