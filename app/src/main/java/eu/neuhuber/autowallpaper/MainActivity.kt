package eu.neuhuber.autowallpaper

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import eu.neuhuber.autowallpaper.ui.SettingsScreen
import eu.neuhuber.autowallpaper.ui.WallpaperUiEvent
import eu.neuhuber.autowallpaper.ui.WallpaperViewModel
import eu.neuhuber.autowallpaper.ui.theme.AutoWallpaperTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val viewModel: WallpaperViewModel = koinViewModel()

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

            AutoWallpaperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        viewModel.bitmap?.let {
                            Image(
                                bitmap = it,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        SettingsScreen(
                            settings = viewModel.settings,
                            isLoading = viewModel.isUpdateInProgress,
                            onSettingsChange = { viewModel.updateSettings(it) },
                            onDownload = { viewModel.downloadImage() },
                            onSetNow = {
                                viewModel.applyWallpaper(context)
                            }
                        )
                    }
                }
            }
        }
    }
}
