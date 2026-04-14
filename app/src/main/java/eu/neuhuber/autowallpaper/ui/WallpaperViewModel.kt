package eu.neuhuber.autowallpaper.ui

import android.app.WallpaperManager
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import eu.neuhuber.autowallpaper.data.BingImageProvider
import eu.neuhuber.autowallpaper.model.WallpaperSettings
import eu.neuhuber.autowallpaper.util.applyWallpaper
import eu.neuhuber.autowallpaper.worker.WallpaperWorker
import timber.log.Timber
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

sealed class WallpaperUiEvent {
    object Success : WallpaperUiEvent()
    data class Error(val message: String) : WallpaperUiEvent()
}

class WallpaperViewModel : ViewModel() {
    var bitmap by mutableStateOf<ImageBitmap?>(null)
        private set
    
    var settings by mutableStateOf(WallpaperSettings())
        private set

    var isUpdateInProgress by mutableStateOf(false)
        private set

    private val _eventChannel = Channel<WallpaperUiEvent>()
    val events = _eventChannel.receiveAsFlow()

    fun updateSettings(newSettings: WallpaperSettings, context: Context) {
        Timber.d("Updating settings: $newSettings")
        settings = newSettings
        scheduleWallpaperWork(context)
    }

    private fun scheduleWallpaperWork(context: Context) {
        Timber.d("Scheduling periodic wallpaper work")
        val workManager = WorkManager.getInstance(context)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "DailyWallpaperUpdate",
            ExistingPeriodicWorkPolicy.UPDATE,
            repeatingRequest
        )
    }

    fun downloadImage() {
        if (isUpdateInProgress) return
        Timber.d("Starting image download")
        viewModelScope.launch {
            isUpdateInProgress = true
            try {
                val image = BingImageProvider.getImage()
                bitmap = image.asImageBitmap()
                Timber.d("Image downloaded successfully (${image.width}x${image.height} ${image.byteCount/1_000_000}MB)")
            } catch (e: Exception) {
                Timber.e(e, "Failed to download image")
                _eventChannel.send(WallpaperUiEvent.Error("Failed to download image"))
            } finally {
                isUpdateInProgress = false
            }
        }
    }

    fun applyWallpaper(context: Context) {
        val currentBitmap = bitmap?.asAndroidBitmap()
        if (currentBitmap == null) {
            Timber.w("Cannot apply wallpaper: bitmap is null")
            return
        }
        Timber.d("Applying wallpaper")
        viewModelScope.launch {
            isUpdateInProgress = true
            try {
                val wallpaperManager = WallpaperManager.getInstance(context)
                applyWallpaper(context, wallpaperManager, currentBitmap, settings)
                Timber.d("Wallpaper applied successfully")
                _eventChannel.send(WallpaperUiEvent.Success)
            } catch (e: Exception) {
                Timber.e(e, "Error applying wallpaper")
                _eventChannel.send(WallpaperUiEvent.Error(e.message ?: "Unknown error"))
            } finally {
                isUpdateInProgress = false
            }
        }
    }
}
