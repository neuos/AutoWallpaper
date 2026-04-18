package eu.neuhuber.autowallpaper

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import eu.neuhuber.autowallpaper.data.WallpaperService
import eu.neuhuber.autowallpaper.ui.MainScreen
import eu.neuhuber.autowallpaper.ui.WallpaperUiEvent
import eu.neuhuber.autowallpaper.ui.WallpaperViewModel
import eu.neuhuber.autowallpaper.ui.theme.AutoWallpaperTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private val wallpaperService: WallpaperService by inject()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val viewModel: WallpaperViewModel = koinViewModel()
            val state = viewModel.state

            LaunchedEffect(key1 = true) {
                if (intent?.action == "eu.neuhuber.autowallpaper.ACTION_REFRESH_WALLPAPER") {
                    wallpaperService.triggerImmediateRefresh()
                }

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

            AutoWallpaperTheme(imageBitmap = state.bitmap) {
                MainScreen(
                    state = state,
                    onDownload = { viewModel.downloadImage() },
                    onApply = { viewModel.applyWallpaper(context) },
                    onUpdateSettings = { viewModel.updateSettings(it) }
                )
            }
        }
    }
}
