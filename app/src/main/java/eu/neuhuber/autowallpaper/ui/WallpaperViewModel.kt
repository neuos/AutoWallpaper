package eu.neuhuber.autowallpaper.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import eu.neuhuber.autowallpaper.data.SettingsRepository
import eu.neuhuber.autowallpaper.data.imageprovider.ImageProviderFactory
import eu.neuhuber.autowallpaper.model.ScheduleMode
import eu.neuhuber.autowallpaper.model.WallpaperSettings
import eu.neuhuber.autowallpaper.util.applyWallpaper
import eu.neuhuber.autowallpaper.worker.WallpaperWorker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

sealed class WallpaperUiEvent {
    object Success : WallpaperUiEvent()
    data class Error(val message: String) : WallpaperUiEvent()
}

class WallpaperViewModel(
    private val repository: SettingsRepository,
    private val workManager: WorkManager,
    private val providerFactory: ImageProviderFactory
) : ViewModel() {

    var bitmap by mutableStateOf<ImageBitmap?>(null)
        private set
    
    var settings by mutableStateOf(WallpaperSettings())
        private set

    var isUpdateInProgress by mutableStateOf(false)
        private set

    private val _eventChannel = Channel<WallpaperUiEvent>()
    val events = _eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collectLatest {
                Timber.d("New settings received from repository: $it")
                settings = it
                scheduleWallpaperWork()
            }
        }
    }

    fun updateSettings(newSettings: WallpaperSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    private fun scheduleWallpaperWork() {
        val schedule = settings.schedule
        if (schedule == ScheduleMode.NONE) {
            Timber.d("Schedule is OFF, canceling any existing work")
            workManager.cancelUniqueWork("DailyWallpaperUpdate")
            return
        }

        Timber.d("Scheduling periodic wallpaper work")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val repeatingRequest = when (schedule) {
            ScheduleMode.HOURLY -> PeriodicWorkRequestBuilder<WallpaperWorker>(1, TimeUnit.HOURS)
            ScheduleMode.DAILY -> PeriodicWorkRequestBuilder<WallpaperWorker>(1, TimeUnit.DAYS)
        }.setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "DailyWallpaperUpdate",
            ExistingPeriodicWorkPolicy.UPDATE,
            repeatingRequest
        )
    }

    fun downloadImage() {
        if (isUpdateInProgress) return
        Timber.d("Starting image download from provider: ${settings.provider}")
        viewModelScope.launch {
            isUpdateInProgress = true
            try {
                val provider = providerFactory.getProvider(settings.provider)
                val image = provider.getImage()
                bitmap = image.asImageBitmap()
                Timber.d("Image downloaded successfully from ${settings.provider} (${image.width}x${image.height} ${image.byteCount / 1_000_000}MB)")
            } catch (e: Exception) {
                Timber.e(e, "Failed to download image from ${settings.provider}")
                _eventChannel.send(WallpaperUiEvent.Error("Failed to download image from ${settings.provider}"))
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
                applyWallpaper(context, currentBitmap, settings)
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
