package com.spine.musicplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.spine.musicplayer.ui.PlayerScreen
import com.spine.musicplayer.ui.theme.SpineTheme
import com.spine.musicplayer.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpineTheme {
                val uiState by playerViewModel.uiState.collectAsState()

                val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                val permissionState = rememberPermissionState(permission = audioPermission)

                LaunchedEffect(permissionState.status.isGranted) {
                    if (permissionState.status.isGranted) {
                        if (playerViewModel.uiState.value.releases.isEmpty()) {
                            playerViewModel.loadLocalMusic()
                        }
                    } else {
                        permissionState.launchPermissionRequest()
                    }
                }

                PlayerScreen(
                    uiState = uiState,
                    onSelectRelease = playerViewModel::selectRelease,
                    onSelectTrack = playerViewModel::selectTrack,
                    onPlayPause = playerViewModel::togglePlayPause,
                    onNext = playerViewModel::nextTrack,
                    onPrevious = playerViewModel::previousTrack,
                    onSeek = playerViewModel::seekTo,
                    onToggleShuffle = playerViewModel::toggleShuffle,
                    onCycleRepeat = playerViewModel::cycleRepeatMode,
                    onRefresh = playerViewModel::loadLocalMusic,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
