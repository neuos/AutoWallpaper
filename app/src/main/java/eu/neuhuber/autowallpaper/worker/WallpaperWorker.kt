package eu.neuhuber.autowallpaper.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eu.neuhuber.autowallpaper.data.SettingsRepository
import eu.neuhuber.autowallpaper.data.imageprovider.ImageProvider
import eu.neuhuber.autowallpaper.util.applyWallpaper
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import timber.log.Timber

class WallpaperWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val repository: SettingsRepository
) : CoroutineWorker(context, workerParams), KoinComponent {

    override suspend fun doWork(): Result {
        return try {
            val settings = repository.settingsFlow.first()
            val provider: ImageProvider by inject(named(settings.provider))
            val bitmap = provider.getImage()

            applyWallpaper(applicationContext, bitmap, settings)
            Timber.d("Wallpaper updated successfully from ${settings.provider}")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error updating wallpaper")
            Result.retry()
        }
    }
}
