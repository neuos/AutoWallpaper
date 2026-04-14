package eu.neuhuber.autowallpaper.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import eu.neuhuber.autowallpaper.model.HomescreenMode
import eu.neuhuber.autowallpaper.model.LockscreenMode
import eu.neuhuber.autowallpaper.model.WallpaperSettings

@Composable
fun SettingsScreen(
    settings: WallpaperSettings,
    isLoading: Boolean,
    onSettingsChange: (WallpaperSettings) -> Unit,
    onDownload: () -> Unit,
    onSetNow: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .statusBarsPadding(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Homescreen", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.selectableGroup()) {
                HomescreenMode.entries.forEach { mode ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (settings.homescreen == mode),
                                onClick = { onSettingsChange(settings.copy(homescreen = mode)) },
                                role = Role.RadioButton,
                                enabled = !isLoading
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (settings.homescreen == mode), onClick = null, enabled = !isLoading)
                        Text(mode.label, Modifier.padding(start = 4.dp))
                    }
                }
            }

            HorizontalDivider()

            Text("Lockscreen", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.selectableGroup()) {
                LockscreenMode.entries.forEach { mode ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (settings.lockscreen == mode),
                                onClick = { onSettingsChange(settings.copy(lockscreen = mode)) },
                                role = Role.RadioButton,
                                enabled = !isLoading
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (settings.lockscreen == mode), onClick = null, enabled = !isLoading)
                        Text(mode.label, Modifier.padding(start = 4.dp))
                    }
                }
            }

            HorizontalDivider()

            Text("Schedule: ${settings.schedule}", style = MaterialTheme.typography.titleMedium)
            Text("Provider: ${settings.provider}", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDownload, modifier = Modifier.weight(1f), enabled = !isLoading) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Download")
                }
                Button(onClick = onSetNow, modifier = Modifier.weight(1f), enabled = !isLoading) {
                    Text("Set Now")
                }
            }
        }
    }
}
