package eu.neuhuber.autowallpaper

import android.app.Application
import eu.neuhuber.autowallpaper.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import timber.log.Timber

class AutoWallpaperApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidLogger()
            androidContext(this@AutoWallpaperApp)
            workManagerFactory()
            modules(appModule)
        }
    }
}
