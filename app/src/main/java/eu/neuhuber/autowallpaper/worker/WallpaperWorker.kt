package eu.neuhuber.autowallpaper.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eu.neuhuber.autowallpaper.data.WallpaperService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class WallpaperWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val wallpaperService: WallpaperService by inject()

    override suspend fun doWork(): Result {
        return try {
            wallpaperService.refreshWallpaper()
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error updating wallpaper in worker")
            Result.retry()
        }
    }
}
