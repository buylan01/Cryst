package com.prolan.catu.ui.screen.home.dialog

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import java.io.File

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AudioPlayer(
    onDismiss: () -> Unit,
    targetFile: File
) {
    BasicAlertDialog(
        onDismissRequest = { onDismiss() },
        content = {
            val context = LocalContext.current
            val exoPlayer = remember {
                ExoPlayer.Builder(context).build().apply {
                    val mediaItem = MediaItem.fromUri(Uri.fromFile(targetFile))
                    setMediaItem(mediaItem)
                    prepare()
                }
            }

            var isPlaying by remember { mutableStateOf(true) }
            var isLooping by remember { mutableStateOf(false) }
            var playbackSpeed by remember { mutableFloatStateOf(1f) }
            var currentPosition by remember { mutableLongStateOf(0L) }
            var totalDuration by remember { mutableLongStateOf(0L) }

            DisposableEffect(Unit) {
                onDispose {
                    exoPlayer.release()
                }
            }

            LaunchedEffect(exoPlayer) {
                exoPlayer.play()
                while (true) {
                    currentPosition = exoPlayer.currentPosition
                    totalDuration = exoPlayer.duration
                    if (totalDuration in 1..currentPosition && !isLooping) {
                        exoPlayer.pause()
                        isPlaying = false
                    }
                    delay(1)
                }
            }

            Surface(
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        targetFile.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Slider(
                            value = if (totalDuration > 0) currentPosition.toFloat()
                                .coerceIn(0f, totalDuration.toFloat()) else 0f,
                            onValueChange = { exoPlayer.seekTo(it.toLong()) },
                            valueRange = if (totalDuration > 0) 0f..totalDuration.toFloat() else 0f..0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = {
                                playbackSpeed = when (playbackSpeed) {
                                    0.5f -> 1f
                                    1f -> 1.5f
                                    1.5f -> 2f
                                    else -> 0.5f
                                }
                                exoPlayer.setPlaybackSpeed(playbackSpeed)
                            }) {
                                Text("$playbackSpeed x")
                            }

                            IconButton(onClick = {
                                isLooping = !isLooping
                                exoPlayer.repeatMode =
                                    if (isLooping) ExoPlayer.REPEAT_MODE_ONE else ExoPlayer.REPEAT_MODE_OFF
                            }) {
                                Icon(
                                    imageVector = if (isLooping) Icons.Default.RepeatOn else Icons.Default.Repeat,
                                    contentDescription = if (isLooping) "关闭循环" else "开启循环"
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                    isPlaying = !isPlaying
                                    if (totalDuration in 1..currentPosition) {
                                        exoPlayer.seekTo(0)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                )
                            }

                            IconButton(
                                onClick = {
                                    onDismiss()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}