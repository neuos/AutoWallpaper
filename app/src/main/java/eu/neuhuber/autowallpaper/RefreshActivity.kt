package eu.neuhuber.autowallpaper

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import eu.neuhuber.autowallpaper.data.WallpaperService
import org.koin.android.ext.android.inject

class RefreshActivity : ComponentActivity() {
    private val wallpaperService: WallpaperService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "Refreshing wallpaper...", Toast.LENGTH_LONG).show()
        wallpaperService.triggerImmediateRefresh()
        finish()
    }

    companion object {
        const val ACTION_REFRESH_WALLPAPER = "eu.neuhuber.autowallpaper.ACTION_REFRESH_WALLPAPER"
    }
}
