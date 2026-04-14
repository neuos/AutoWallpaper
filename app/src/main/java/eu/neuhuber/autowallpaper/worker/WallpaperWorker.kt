package eu.neuhuber.autowallpaper.worker

import android.app.WallpaperManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eu.neuhuber.autowallpaper.data.BingImageProvider
import eu.neuhuber.autowallpaper.data.SettingsRepository
import eu.neuhuber.autowallpaper.util.applyWallpaper
import timber.log.Timber
import kotlinx.coroutines.flow.first

class WallpaperWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val repository = SettingsRepository(applicationContext)
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
