package eu.neuhuber.autowallpaper

import android.app.Application
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.work.Configuration
import eu.neuhuber.autowallpaper.di.appModule
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import timber.log.Timber

class AutoWallpaperApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(getKoin().get<KoinWorkerFactory>())
            .build()

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

        setupDynamicShortcuts()
    }

    private fun setupDynamicShortcuts() {
        val shortcut = ShortcutInfoCompat.Builder(this, "refresh_dynamic_v2")
            .setShortLabel(getString(R.string.shortcut_refresh_label))
            .setLongLabel(getString(R.string.shortcut_refresh_long_label))
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_refresh))
            .setIntent(
                Intent(this, RefreshActivity::class.java).apply {
                    action = RefreshActivity.ACTION_REFRESH_WALLPAPER
                }
            )
            .build()

        ShortcutManagerCompat.removeAllDynamicShortcuts(this)
        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)
    }
}
