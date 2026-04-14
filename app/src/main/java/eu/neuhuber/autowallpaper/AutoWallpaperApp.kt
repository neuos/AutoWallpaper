package eu.neuhuber.autowallpaper

import android.app.Application
import timber.log.Timber

class AutoWallpaperApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
