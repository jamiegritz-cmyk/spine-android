package com.spine.musicplayer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.spine.musicplayer.data.MusicFolderPreferences
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
                var currentFolderDisplay by remember {
                    mutableStateOf(MusicFolderPreferences.getSelectedFolderName(this@MainActivity))
                }

                val folderPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocumentTree()
                ) { uri ->
                    if (uri != null) {
                        try {
                            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            contentResolver.takePersistableUriPermission(uri, takeFlags)
                        } catch (_: SecurityException) {
                            // Proceed even if persistable permission is already granted or restricted
                        }
                        playerViewModel.onFolderSelected(uri)
                        currentFolderDisplay = MusicFolderPreferences.getSelectedFolderName(this@MainActivity)
                    }
                }

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
                    onRefresh = {
                        playerViewModel.loadLocalMusic()
                        currentFolderDisplay = MusicFolderPreferences.getSelectedFolderName(this@MainActivity)
                    },
                    onSelectFolder = {
                        folderPickerLauncher.launch(null)
                    },
                    selectedFolderName = currentFolderDisplay,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
