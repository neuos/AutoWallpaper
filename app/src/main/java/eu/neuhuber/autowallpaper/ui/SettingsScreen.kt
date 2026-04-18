package eu.neuhuber.autowallpaper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.neuhuber.autowallpaper.model.HomescreenMode
import eu.neuhuber.autowallpaper.model.LockscreenMode
import eu.neuhuber.autowallpaper.model.ScheduleMode
import eu.neuhuber.autowallpaper.model.WallpaperSettings

@Composable
fun SettingsContent(
    settings: WallpaperSettings,
    isLoading: Boolean,
    onSettingsChange: (WallpaperSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Homescreen", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            HomescreenMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index, count = HomescreenMode.entries.size
                    ),
                    onClick = { onSettingsChange(settings.copy(homescreen = mode)) },
                    selected = settings.homescreen == mode,
                    label = { Text(mode.label) },
                    enabled = !isLoading
                )
            }
        }


        Text("Lockscreen", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            LockscreenMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index, count = LockscreenMode.entries.size
                    ),
                    onClick = { onSettingsChange(settings.copy(lockscreen = mode)) },
                    selected = settings.lockscreen == mode,
                    label = { Text(mode.label) },
                    enabled = !isLoading
                )
            }
        }


        val providers = listOf("Bing", "Picsum")
        Text("Provider", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            providers.forEachIndexed { index, provider ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index, count = providers.size
                    ),
                    onClick = { onSettingsChange(settings.copy(provider = provider)) },
                    selected = settings.provider == provider,
                    label = { Text(provider) },
                    enabled = !isLoading
                )
            }
        }


        Text("Schedule", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ScheduleMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index, count = ScheduleMode.entries.size
                    ),
                    onClick = { onSettingsChange(settings.copy(schedule = mode)) },
                    selected = settings.schedule == mode,
                    label = { Text(mode.label) },
                    enabled = !isLoading
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    isLoading: Boolean,
    onDownload: () -> Unit,
    onSetNow: () -> Unit,
    onShowSettings: () -> Unit,
    isSetNowEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onDownload,
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                else Text("Preview")
            }
            Button(
                onClick = onSetNow,
                modifier = Modifier.weight(1f),
                enabled = !isLoading && isSetNowEnabled
            ) {
                Text("Set")
            }
            IconButton(
                onClick = onShowSettings,
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    }
}
