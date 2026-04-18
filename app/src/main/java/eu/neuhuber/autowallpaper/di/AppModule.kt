package eu.neuhuber.autowallpaper.di

import androidx.work.WorkManager
import eu.neuhuber.autowallpaper.data.SettingsRepository
import eu.neuhuber.autowallpaper.data.imageprovider.BingImageProvider
import eu.neuhuber.autowallpaper.data.imageprovider.ImageProvider
import eu.neuhuber.autowallpaper.data.imageprovider.PicsumImageProvider
import eu.neuhuber.autowallpaper.ui.WallpaperViewModel
import eu.neuhuber.autowallpaper.worker.WallpaperWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    singleOf(::SettingsRepository)

    single<ImageProvider>(named("Bing")) { BingImageProvider }
    single<ImageProvider>(named("Picsum")) { PicsumImageProvider }

    single {
        mapOf(
            "Bing" to get<ImageProvider>(named("Bing")),
            "Picsum" to get<ImageProvider>(named("Picsum"))
        )
    }

    single { WorkManager.getInstance(get()) }
    viewModelOf(::WallpaperViewModel)
    workerOf(::WallpaperWorker)
}
