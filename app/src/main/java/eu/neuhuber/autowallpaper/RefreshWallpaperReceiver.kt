package eu.neuhuber.autowallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import eu.neuhuber.autowallpaper.data.WallpaperService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class RefreshWallpaperReceiver : BroadcastReceiver(), KoinComponent {
    private val wallpaperService: WallpaperService by inject()

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("Received broadcast: ${intent.action}")
        if (intent.action == ACTION_REFRESH_WALLPAPER) {
            wallpaperService.triggerImmediateRefresh()
        }
    }

    companion object {
        const val ACTION_REFRESH_WALLPAPER = "eu.neuhuber.autowallpaper.ACTION_REFRESH_WALLPAPER"
    }
}
