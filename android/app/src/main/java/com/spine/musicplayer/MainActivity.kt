package com.spine.musicplayer

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.spine.musicplayer.ui.PlayerScreen
import com.spine.musicplayer.ui.theme.SpineTheme
import com.spine.musicplayer.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private var startupError by mutableStateOf<Throwable?>(null)
    private var viewModel: PlayerViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Capture any fatal exception before or during ViewModel creation
        try {
            viewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        } catch (t: Throwable) {
            Log.e("GRAIZ_STARTUP", "Failed to create PlayerViewModel", t)
            startupError = t
        }

        setContent {
            SpineTheme {
                val error = startupError ?: GraizApplication.fatalCrash
                val vm = viewModel

                if (error != null || vm == null) {
                    DiagnosticCrashScreen(
                        throwable = error ?: RuntimeException("PlayerViewModel failed to initialize"),
                        onRetry = {
                            try {
                                viewModel = ViewModelProvider(this@MainActivity)[PlayerViewModel::class.java]
                                startupError = null
                                GraizApplication.fatalCrash = null
                            } catch (t: Throwable) {
                                startupError = t
                            }
                        }
                    )
                } else {
                    GraizMainContent(playerViewModel = vm)
                }
            }
        }
    }

    @Composable
    private fun GraizMainContent(playerViewModel: PlayerViewModel) {
        val uiState by playerViewModel.uiState.collectAsState()

        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                try {
                    playerViewModel.loadLocalMusic()
                } catch (t: Throwable) {
                    Log.e("GRAIZ", "Error in loadLocalMusic callback", t)
                }
            }
        }

        LaunchedEffect(Unit) {
            try {
                val isGranted = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    audioPermission
                ) == PackageManager.PERMISSION_GRANTED

                if (isGranted) {
                    playerViewModel.loadLocalMusic()
                } else {
                    try {
                        permissionLauncher.launch(audioPermission)
                    } catch (t: Throwable) {
                        Log.e("GRAIZ", "Could not launch permission request", t)
                    }
                }
            } catch (t: Throwable) {
                Log.e("GRAIZ", "Error checking audio permissions", t)
            }
        }

        PlayerScreen(
            uiState = uiState,
            onSelectRelease = playerViewModel::selectRelease,
            onPlayPause = playerViewModel::togglePlayPause,
            onNext = playerViewModel::nextTrack,
            onPrevious = playerViewModel::previousTrack,
            onSeek = playerViewModel::seekTo,
            onToggleShuffle = playerViewModel::toggleShuffle,
            onCycleRepeat = playerViewModel::cycleRepeatMode,
            onRefresh = playerViewModel::refreshLibrary,
            onFilterChange = playerViewModel::setFilterMode,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Diagnostic crash screen that renders exception details directly on screen
 * if a startup or initialisation failure occurs, preventing immediate closure.
 */
@Composable
fun DiagnosticCrashScreen(
    throwable: Throwable,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val stackTrace = throwable.stackTraceToString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E0D))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GRAIZ",
            style = MaterialTheme.typography.headlineMedium.copy(
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF5F5F4)
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Startup Diagnostic",
            style = MaterialTheme.typography.titleSmall.copy(
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1D1B), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Exception: ${throwable.javaClass.simpleName}",
                    color = Color(0xFFFCA5A5),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = throwable.message ?: "No error message provided",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stackTrace.lines().take(12).joinToString("\n"),
                    color = Color(0xFFA8A29E),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val clip = ClipData.newPlainText("GRAIZ Error", stackTrace)
                    clipboard?.setPrimaryClip(clip)
                    Toast.makeText(context, "Error copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF292524)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Copy Error", color = Color.White)
            }

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD6D3D1)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
