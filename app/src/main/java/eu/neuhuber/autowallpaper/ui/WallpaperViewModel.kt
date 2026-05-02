package eu.neuhuber.autowallpaper.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.neuhuber.autowallpaper.data.SettingsRepository
import eu.neuhuber.autowallpaper.data.WallpaperService
import eu.neuhuber.autowallpaper.model.WallpaperSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import timber.log.Timber

sealed class WallpaperUiEvent {
    object Success : WallpaperUiEvent()
    data class Error(val message: String) : WallpaperUiEvent()
}

data class WallpaperUiState(
    val bitmap: ImageBitmap? = null,
    val seedColor: Color? = null,
    val settings: WallpaperSettings = WallpaperSettings(),
    val isUpdateInProgress: Boolean = false
)

class WallpaperViewModel(
    private val repository: SettingsRepository,
    private val wallpaperService: WallpaperService
) : ViewModel(), KoinComponent {

    var state by mutableStateOf(WallpaperUiState())
        private set

    private val _eventChannel = Channel<WallpaperUiEvent>()
    val events = _eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collectLatest { settings ->
                Timber.d("New settings received from repository: $settings")
                val oldSettings = state.settings
                state = state.copy(settings = settings)
                wallpaperService.scheduleWallpaperWork(settings)

                // Automatically download image on first load or if provider/homescreen mode changed
                if (state.bitmap == null ||
                    oldSettings.provider != settings.provider ||
                    oldSettings.homescreen != settings.homescreen
                ) {
                    downloadImage()
                }
            }
        }
    }

    fun updateSettings(newSettings: WallpaperSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    fun downloadImage() {
        if (state.isUpdateInProgress) return
        Timber.d("Starting image download from provider: ${state.settings.provider}")
        viewModelScope.launch {
            state = state.copy(isUpdateInProgress = true)
            try {
                val image = wallpaperService.fetchImage()
                val seedColor = withContext(Dispatchers.Default) {
                    Color(WallpaperService.extractSeedColor(image))
                }
                state = state.copy(
                    bitmap = image.asImageBitmap(),
                    seedColor = seedColor
                )
                Timber.d("Image downloaded successfully from ${state.settings.provider} (${image.width}x${image.height} ${image.byteCount / 1_000_000}MB)")
            } catch (e: Exception) {
                Timber.e(e, "Failed to download image from ${state.settings.provider}")
                _eventChannel.send(WallpaperUiEvent.Error("Failed to download image from ${state.settings.provider}"))
            } finally {
                state = state.copy(isUpdateInProgress = false)
            }
        }
    }

    fun applyWallpaper() {
        val currentBitmap = state.bitmap?.asAndroidBitmap()
        if (currentBitmap == null) {
            Timber.w("Cannot apply wallpaper: bitmap is null")
            return
        }
        Timber.d("Applying wallpaper")
        viewModelScope.launch {
            state = state.copy(isUpdateInProgress = true)
            try {
                wallpaperService.applyWallpaper(currentBitmap, state.settings)
                Timber.d("Wallpaper applied successfully")
                _eventChannel.send(WallpaperUiEvent.Success)
            } catch (e: Exception) {
                Timber.e(e, "Error applying wallpaper")
                _eventChannel.send(WallpaperUiEvent.Error(e.message ?: "Unknown error"))
            } finally {
                state = state.copy(isUpdateInProgress = false)
            }
        }
    }
}