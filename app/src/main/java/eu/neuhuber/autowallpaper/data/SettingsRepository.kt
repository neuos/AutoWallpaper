package eu.neuhuber.autowallpaper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import eu.neuhuber.autowallpaper.model.HomescreenMode
import eu.neuhuber.autowallpaper.model.LockscreenMode
import eu.neuhuber.autowallpaper.model.WallpaperSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val HOMESCREEN_MODE = stringPreferencesKey("homescreen_mode")
        val LOCKSCREEN_MODE = stringPreferencesKey("lockscreen_mode")
        val PROVIDER = stringPreferencesKey("provider")
        val SCHEDULE = stringPreferencesKey("schedule")
    }

    val settingsFlow: Flow<WallpaperSettings> = context.dataStore.data
        .map { preferences ->
            val homescreen = try {
                HomescreenMode.valueOf(preferences[PreferencesKeys.HOMESCREEN_MODE] ?: HomescreenMode.SCROLLING.name)
            } catch (e: IllegalArgumentException) {
                HomescreenMode.SCROLLING
            }

            val lockscreen = try {
                LockscreenMode.valueOf(preferences[PreferencesKeys.LOCKSCREEN_MODE] ?: LockscreenMode.YES.name)
            } catch (e: IllegalArgumentException) {
                LockscreenMode.YES
            }

            WallpaperSettings(
                homescreen = homescreen,
                lockscreen = lockscreen,
                provider = preferences[PreferencesKeys.PROVIDER] ?: "Bing",
                schedule = preferences[PreferencesKeys.SCHEDULE] ?: "Daily"
            )
        }

    suspend fun updateSettings(settings: WallpaperSettings) {
        Timber.d("Saving settings to DataStore: $settings")
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HOMESCREEN_MODE] = settings.homescreen.name
            preferences[PreferencesKeys.LOCKSCREEN_MODE] = settings.lockscreen.name
            preferences[PreferencesKeys.PROVIDER] = settings.provider
            preferences[PreferencesKeys.SCHEDULE] = settings.schedule
        }
    }
}
