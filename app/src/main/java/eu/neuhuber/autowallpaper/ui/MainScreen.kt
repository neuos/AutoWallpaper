package eu.neuhuber.autowallpaper.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: WallpaperUiState,
    onDownload: () -> Unit,
    onApply: () -> Unit,
    onUpdateSettings: (eu.neuhuber.autowallpaper.model.WallpaperSettings) -> Unit,
    modifier: Modifier = Modifier,
    initialShowSettings: Boolean = true
) {
    var showSettings by rememberSaveable { mutableStateOf(initialShowSettings) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(modifier = modifier
        .fillMaxSize()
        .testTag("main_screen")) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            state.bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            ActionButtons(
                modifier = Modifier.padding(innerPadding),
                isLoading = state.isUpdateInProgress,
                isSetNowEnabled = state.bitmap != null,
                onDownload = onDownload,
                onSetNow = onApply,
                onShowSettings = { showSettings = true }
            )

            if (showSettings) {
                ModalBottomSheet(
                    onDismissRequest = { showSettings = false },
                    sheetState = sheetState
                ) {
                    SettingsContent(
                        settings = state.settings,
                        isLoading = state.isUpdateInProgress,
                        onSettingsChange = onUpdateSettings,
                        modifier = Modifier.testTag("settings_content")
                    )
                }
            }
        }
    }
}
