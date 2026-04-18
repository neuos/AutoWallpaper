package eu.neuhuber.autowallpaper.data

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import eu.neuhuber.autowallpaper.model.ScheduleMode
import eu.neuhuber.autowallpaper.model.WallpaperSettings
import eu.neuhuber.autowallpaper.worker.WallpaperWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WallpaperService(
    private val workManager: WorkManager
) {
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

    fun refreshWallpaperImmediately() {
        Timber.d("Refreshing wallpaper immediately via OneTimeWorkRequest")
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
    }
}