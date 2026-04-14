package eu.neuhuber.autowallpaper.di

import androidx.work.WorkManager
import eu.neuhuber.autowallpaper.data.SettingsRepository
import eu.neuhuber.autowallpaper.data.imageprovider.ImageProviderFactory
import eu.neuhuber.autowallpaper.ui.WallpaperViewModel
import eu.neuhuber.autowallpaper.worker.WallpaperWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::SettingsRepository)
    singleOf(::ImageProviderFactory)
    single { WorkManager.getInstance(get()) }
    viewModelOf(::WallpaperViewModel)
    workerOf(::WallpaperWorker)
}
