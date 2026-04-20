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
import eu.neuhuber.autowallpaper.data.imageprovider.PicsumImageProvider
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
            val provider: ImageProvider = get(named(settings.provider))
            if (provider is PicsumImageProvider) {
                provider.resetSeed()
            }
            applySettingsInternal(settings, provider, null)
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
        val provider: ImageProvider = get(named(settings.provider))
        applySettingsInternal(settings, provider, bitmap)
    }

    private suspend fun applySettingsInternal(
        settings: WallpaperSettings,
        provider: ImageProvider,
        initialBitmap: Bitmap?
    ) {
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        // If we have an initial bitmap and it's portrait, we can use it for anything that needs portrait.
        // If it's wide, we'll only use it for scrolling home screen.
        var portraitBitmap: Bitmap? =
            if (initialBitmap != null && initialBitmap.width <= initialBitmap.height) initialBitmap else null

        // Home screen
        if (settings.homescreen != HomescreenMode.NONE) {
            val isScrolling = settings.homescreen == HomescreenMode.SCROLLING
            val baseWidth = if (isScrolling) screenWidth * 3 else screenWidth
            val baseHeight = screenHeight
            val ratio = baseWidth.toFloat() / baseHeight

            val bitmapToUse = if (isScrolling) {
                if (initialBitmap != null && initialBitmap.width > initialBitmap.height) {
                    initialBitmap
                } else {
                    Timber.d("Fetching wide image for scrolling home screen")
                    val (fetchWidth, fetchHeight) = getCappedDimensions(
                        baseWidth * 2,
                        baseHeight * 2
                    )
                    provider.getImage(fetchWidth, fetchHeight)
                }
            } else {
                val currentPortrait = portraitBitmap ?: run {
                    Timber.d("Fetching portrait image for home screen")
                    val (fetchWidth, fetchHeight) = getCappedDimensions(
                        screenWidth * 2,
                        screenHeight * 2
                    )
                    val newBitmap = provider.getImage(fetchWidth, fetchHeight)
                    portraitBitmap = newBitmap
                    newBitmap
                }
                currentPortrait
            }
            setWallpaper(bitmapToUse, WallpaperManager.FLAG_SYSTEM, ratio)
        }

        // Lock screen
        if (settings.lockscreen == LockscreenMode.YES) {
            val currentPortrait = portraitBitmap ?: run {
                Timber.d("Fetching portrait image for lock screen (requested from server)")
                val (fetchWidth, fetchHeight) = getCappedDimensions(
                    screenWidth * 2,
                    screenHeight * 2
                )
                provider.getImage(fetchWidth, fetchHeight)
            }
            val ratio = screenWidth.toFloat() / screenHeight
            setWallpaper(currentPortrait, WallpaperManager.FLAG_LOCK, ratio)
        }
    }

    /**
     * Mid-level: Fetch image from current provider.
     */
    suspend fun fetchImage(): Bitmap {
        val settings = repository.settingsFlow.first()
        val provider: ImageProvider = get(named(settings.provider))

        if (provider is PicsumImageProvider) {
            provider.resetSeed()
        }

        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        val isScrolling = settings.homescreen == HomescreenMode.SCROLLING
        val baseWidth = if (isScrolling) screenWidth * 3 else screenWidth
        val baseHeight = screenHeight

        val (fetchWidth, fetchHeight) = getCappedDimensions(baseWidth * 2, baseHeight * 2)

        return provider.getImage(fetchWidth, fetchHeight)
    }

    private fun getCappedDimensions(width: Int, height: Int): Pair<Int, Int> {
        val maxDim = 4000
        if (width <= maxDim && height <= maxDim) return width to height
        val ratio = width.toDouble() / height
        return if (width > height) {
            maxDim to (maxDim / ratio).toInt()
        } else {
            (maxDim * ratio).toInt() to maxDim
        }
    }

    /**
     * Low-level: Apply bitmap to system.
     */
    private suspend fun setWallpaper(image: Bitmap, which: Int, aspectRatio: Float) {
        val manager = WallpaperManager.getInstance(context)
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
