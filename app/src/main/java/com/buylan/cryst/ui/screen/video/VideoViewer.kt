/*
 *    Copyright 2026 buylan
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.buylan.cryst.ui.screen.video

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.material3.Player
import com.buylan.cryst.R
import java.io.File

@androidx.annotation.OptIn(UnstableApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "OpaqueUnitKey")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoViewer(
    filePath: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val file = File(filePath)

    val backgroundColors = listOf(
        Color.Black,
        Color.Gray,
        Color.White
    )
    var currentBgColorIndex by remember { mutableIntStateOf(0) }
    val currentBgColor = backgroundColors[currentBgColorIndex]

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
            prepare()
            play()
        }
    }

    var showControls by remember { mutableStateOf(false) }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.displayCutout,
        topBar = {
            if (showControls) {
                TopAppBar(
                    title = {
                        Text(
                            text = file.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                currentBgColorIndex =
                                    (currentBgColorIndex + 1) % backgroundColors.size
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_invert_colors),
                                contentDescription = null
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        },
        containerColor = currentBgColor
    ) { _ ->
        Box {
            Player(
                modifier = Modifier
                    .padding()
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = null,
                        onClick = {
                            showControls = !showControls
                        }
                    ),
                contentScale = ContentScale.Fit,
                player = player,
                showControls = showControls,
                topControls = null
            )
        }
    }
}