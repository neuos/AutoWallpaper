package eu.neuhuber.autowallpaper.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import eu.neuhuber.autowallpaper.data.imageprovider.ImageProvider
import eu.neuhuber.autowallpaper.model.HomescreenMode
import eu.neuhuber.autowallpaper.model.LockscreenMode
import eu.neuhuber.autowallpaper.model.ScheduleMode
import eu.neuhuber.autowallpaper.model.WallpaperSettings
import eu.neuhuber.autowallpaper.worker.WallpaperWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit

class WallpaperService(
    private val context: Context,
    private val workManager: WorkManager,
    private val repository: SettingsRepository
) : KoinComponent {

    /**
     * High-level: Fetch from provider and apply.
     */
    suspend fun refreshWallpaper() {
        try {
            val settings = repository.settingsFlow.first()
            val bitmap = fetchImage()
            applyWallpaper(bitmap, settings)
            Timber.d("Wallpaper updated successfully from ${settings.provider}")
        } catch (e: Exception) {
            Timber.e(e, "Error updating wallpaper")
            throw e
        }
    }

    /**
     * Mid-level: Apply a given bitmap using current settings.
     */
    suspend fun applyWallpaper(bitmap: Bitmap, settings: WallpaperSettings) {
        val manager = WallpaperManager.getInstance(context)
        if (settings.homescreen != HomescreenMode.NONE) {
            val ratio = if (settings.homescreen == HomescreenMode.SCROLLING) {
                manager.desiredMinimumWidth.toFloat() / manager.desiredMinimumHeight
            } else {
                context.resources.displayMetrics.widthPixels.toFloat() / context.resources.displayMetrics.heightPixels
            }
            setWallpaper(manager, bitmap, WallpaperManager.FLAG_SYSTEM, ratio)
        }

        if (settings.lockscreen == LockscreenMode.YES) {
            val metrics = context.resources.displayMetrics
            val ratio = metrics.widthPixels.toFloat() / metrics.heightPixels
            setWallpaper(manager, bitmap, WallpaperManager.FLAG_LOCK, ratio)
        }
    }

    /**
     * Mid-level: Fetch image from current provider.
     */
    suspend fun fetchImage(): Bitmap {
        val settings = repository.settingsFlow.first()
        val provider: ImageProvider = get(named(settings.provider))
        return provider.getImage()
    }

    /**
     * Low-level: Apply bitmap to system.
     */
    private suspend fun setWallpaper(manager: WallpaperManager, image: Bitmap, which: Int, aspectRatio: Float) {
        if (!manager.isSetWallpaperAllowed) {
            Timber.w("Wallpaper set not allowed")
            return
        }
        try {
            Timber.d("Cropping bitmap for $which with aspect ratio $aspectRatio")
            val cropped = centerCrop(image, aspectRatio)
            withContext(Dispatchers.IO) {
                Timber.d("Setting bitmap for $which")
                manager.setBitmap(cropped, null, true, which)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set wallpaper for $which")
        }
    }

    private suspend fun centerCrop(image: Bitmap, aspectRatio: Float): Bitmap =
        withContext(Dispatchers.Default) {
            val origWidth = image.width
            val origHeight = image.height
            val currentAspectRatio = origWidth.toFloat() / origHeight

            val (cropWidth, cropHeight) = if (currentAspectRatio > aspectRatio) {
                (origHeight * aspectRatio).toInt() to origHeight
            } else {
                origWidth to (origWidth / aspectRatio).toInt()
            }

            val xOffset = (origWidth - cropWidth) / 2
            val yOffset = (origHeight - cropHeight) / 2
            Bitmap.createBitmap(image, xOffset, yOffset, cropWidth, cropHeight)
        }

    /**
     * Scheduling: Manage WorkManager.
     */
    fun scheduleWallpaperWork(settings: WallpaperSettings) {
        val schedule = settings.schedule
        if (schedule == ScheduleMode.NONE) {
            Timber.d("Schedule is OFF, canceling any existing work")
            workManager.cancelUniqueWork("DailyWallpaperUpdate")
            return
        }

        Timber.d("Scheduling periodic wallpaper work: $schedule")
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

    fun triggerImmediateRefresh(): UUID {
        Timber.d("Triggering immediate wallpaper refresh via OneTimeWorkRequest")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<WallpaperWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "ImmediateWallpaperUpdate",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        return workRequest.id
    }

    companion object {
        fun extractSeedColor(bitmap: Bitmap): Int {
            val palette = Palette.from(bitmap).generate()
            return palette.getDominantColor(0)
        }
    }
}