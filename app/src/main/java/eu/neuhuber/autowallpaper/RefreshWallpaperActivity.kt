package eu.neuhuber.autowallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import eu.neuhuber.autowallpaper.data.WallpaperService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RefreshWallpaperActivity : ComponentActivity() {
    private val wallpaperService: WallpaperService by inject()
    private val workManager: WorkManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Trigger the refresh
        wallpaperService.triggerImmediateRefresh()

        // UI: Centered loader
        setContent {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp,
                    strokeCap = StrokeCap.Round
                )
            }
        }

        // Observe the WorkManager status and finish when done
        lifecycleScope.launch {
            workManager.getWorkInfosForUniqueWorkLiveData("ImmediateWallpaperUpdate")
                .asFlow()
                .collectLatest { workInfos ->
                    if (workInfos.isNotEmpty()) {
                        val state = workInfos[0].state
                        if (state.isFinished) {
                            finish()
                        }
                    }
                }
        }
    }
}
