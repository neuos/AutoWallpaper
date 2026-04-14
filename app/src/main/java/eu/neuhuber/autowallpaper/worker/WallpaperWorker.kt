package eu.neuhuber.autowallpaper.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eu.neuhuber.autowallpaper.data.BingImageProvider
import eu.neuhuber.autowallpaper.data.SettingsRepository
import eu.neuhuber.autowallpaper.util.applyWallpaper
import kotlinx.coroutines.flow.first
import timber.log.Timber

class WallpaperWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val repository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val settings = repository.settingsFlow.first()
            val bitmap = BingImageProvider.getImage()

            applyWallpaper(applicationContext, bitmap, settings)
            Timber.d("Wallpaper updated successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error updating wallpaper")
            Result.retry()
        }
    }
}
