package eu.neuhuber.autowallpaper.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import eu.neuhuber.autowallpaper.model.HomescreenMode
import eu.neuhuber.autowallpaper.model.LockscreenMode
import eu.neuhuber.autowallpaper.model.WallpaperSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun applyWallpaper(context: Context, manager: WallpaperManager, bitmap: Bitmap, settings: WallpaperSettings) {
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

suspend fun setWallpaper(manager: WallpaperManager, image: Bitmap, which: Int, aspectRatio: Float) {
    if (!manager.isSetWallpaperAllowed) return
    try {
        val cropped = image.centerCrop(aspectRatio)
        withContext(Dispatchers.IO) {
            manager.setBitmap(cropped, null, true, which)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun Bitmap.centerCrop(aspectRatio: Float): Bitmap =
    withContext(Dispatchers.Default) {
        val origWidth = width
        val origHeight = height
        val currentAspectRatio = origWidth.toFloat() / origHeight

        val (cropWidth, cropHeight) = if (currentAspectRatio > aspectRatio) {
            (origHeight * aspectRatio).toInt() to origHeight
        } else {
            origWidth to (origWidth / aspectRatio).toInt()
        }

        val xOffset = (origWidth - cropWidth) / 2
        val yOffset = (origHeight - cropHeight) / 2
        Bitmap.createBitmap(this@centerCrop, xOffset, yOffset, cropWidth, cropHeight)
    }
