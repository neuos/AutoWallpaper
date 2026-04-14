package eu.neuhuber.autowallpaper.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import eu.neuhuber.autowallpaper.model.HomescreenMode
import eu.neuhuber.autowallpaper.model.LockscreenMode
import eu.neuhuber.autowallpaper.model.WallpaperSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

suspend fun applyWallpaper(context: Context, bitmap: Bitmap, settings: WallpaperSettings) {
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

suspend fun setWallpaper(manager: WallpaperManager, image: Bitmap, which: Int, aspectRatio: Float) {
    if (!manager.isSetWallpaperAllowed) {
        Timber.w("Wallpaper set not allowed")
        return
    }
    try {
        Timber.d("Cropping bitmap for $which with aspect ratio $aspectRatio")
        val cropped = image.centerCrop(aspectRatio)
        withContext(Dispatchers.IO) {
            Timber.d("Setting bitmap for $which")
            manager.setBitmap(cropped, null, true, which)
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to set wallpaper for $which")
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
