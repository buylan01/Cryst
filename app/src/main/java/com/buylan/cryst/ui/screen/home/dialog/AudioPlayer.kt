package com.buylan.cryst.ui.screen.home.dialog

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.buylan.cryst.R
import com.buylan.cryst.util.getAudioMetadata
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.delay
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AudioPlayer(
    onDismiss: () -> Unit,
    targetFile: VirtualFile
) {
    BasicAlertDialog(
        onDismissRequest = { onDismiss() },
        content = {
            val context = LocalContext.current
            val exoPlayer = remember {
                ExoPlayer.Builder(context).build().apply {
                    val mediaItem = MediaItem.fromUri(Uri.fromFile(File(targetFile.absolutePath)))
                    setMediaItem(mediaItem)
                    prepare()
                }
            }

            val metadata = getAudioMetadata(targetFile.absolutePath)

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

            DisposableEffect(exoPlayer) {
                val listener = object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }
                    override fun onPlaybackStateChanged(state: Int) {}
                }
                exoPlayer.addListener(listener)
                onDispose {
                    exoPlayer.removeListener(listener)
                }
            }

            LaunchedEffect(exoPlayer) {
                exoPlayer.play()
                while (true) {
                    currentPosition = exoPlayer.currentPosition
                    totalDuration = exoPlayer.duration
                    delay(10.milliseconds)
                }
            }

            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(0.9f),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {

                    Column {
                        Text(
                            text = metadata?.title ?: targetFile.name,
                            style = MaterialTheme.typography.titleLarge
                        )

                        metadata?.artist?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    metadata?.cover?.let {
                        val ratio = it.width.toFloat() / it.height.toFloat()
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(ratio)
                                .heightIn(max = 280.dp)
                                .clip(MaterialTheme.shapes.large),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        var isDragging by remember { mutableStateOf(false) }
                        var sliderPosition by remember { mutableFloatStateOf(0f) }

                        LaunchedEffect(currentPosition) {
                            if (!isDragging) {
                                sliderPosition = if (totalDuration > 0) currentPosition.toFloat() else 0f
                            }
                        }

                        Slider(
                            value = sliderPosition,
                            onValueChange = {
                                sliderPosition = it
                                if (!isDragging) {
                                    isDragging = true
                                }
                            },
                            onValueChangeFinished = {
                                isDragging = false
                                exoPlayer.seekTo(sliderPosition.toLong())
                            },
                            valueRange = if (totalDuration > 0) 0f..totalDuration.toFloat() else 0f..0f,
                            modifier = Modifier
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    playbackSpeed = when (playbackSpeed) {
                                        0.5f -> 1f
                                        1f -> 1.5f
                                        1.5f -> 2f
                                        else -> 0.5f
                                    }
                                    exoPlayer.setPlaybackSpeed(playbackSpeed)
                                }
                            ) {
                                Text("$playbackSpeed x")
                            }

                            IconButton(
                                onClick = {
                                    isLooping = !isLooping
                                    exoPlayer.repeatMode =
                                        if (isLooping) ExoPlayer.REPEAT_MODE_ONE else ExoPlayer.REPEAT_MODE_OFF
                                }
                            ) {
                                Icon(
                                    painter = painterResource(if (isLooping) R.drawable.ic_repeat_on else R.drawable.ic_repeat),
                                    contentDescription = null
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                    if (totalDuration in 1..currentPosition) {
                                        exoPlayer.seekTo(0)
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                                    contentDescription = null,
                                )
                            }

                            IconButton(
                                onClick = {
                                    onDismiss()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
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