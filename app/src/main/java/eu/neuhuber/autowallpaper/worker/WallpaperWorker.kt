package eu.neuhuber.autowallpaper.worker

import android.app.WallpaperManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import eu.neuhuber.autowallpaper.data.BingImageProvider
import eu.neuhuber.autowallpaper.model.WallpaperSettings
import eu.neuhuber.autowallpaper.util.applyWallpaper

class WallpaperWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val bitmap = BingImageProvider.getImage()
            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            
            // For now, use default settings. In a real app, these would be loaded from DataStore/SharedPreferences.
            val settings = WallpaperSettings() 
            
            applyWallpaper(applicationContext, wallpaperManager, bitmap, settings)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
