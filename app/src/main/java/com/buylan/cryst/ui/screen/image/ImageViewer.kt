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

package com.buylan.cryst.ui.screen.image

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.buylan.cryst.R
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import java.io.File

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewer(
    context: Context,
    filePath: String
) {

    val imageFile = File(filePath)
    val backgroundColors = listOf(
        Color.Black,
        Color.Gray,
        Color.White
    )
    var currentBgColorIndex by remember { mutableIntStateOf(0) }
    val currentBgColor = backgroundColors[currentBgColorIndex]

    Scaffold(
        contentWindowInsets = WindowInsets.displayCutout,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = imageFile.name,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.StartEllipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            (context as ComponentActivity).finish()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            currentBgColorIndex = (currentBgColorIndex + 1) % backgroundColors.size
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
        },
        containerColor = currentBgColor
    ) {

        val builder = ImageRequest.Builder(LocalContext.current)
            .data(imageFile)
            .size(coil.size.Size.ORIGINAL)
            .build()

        val zoomState = rememberZoomState(maxScale = 8f)

        SubcomposeAsyncImage(
            model = builder,
            contentDescription = imageFile.name,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High,
            modifier = Modifier
                .zoomable(zoomState, enableOneFingerZoom = false)
                .fillMaxSize(),
            onSuccess = { state ->
                zoomState.setContentSize(state.painter.intrinsicSize)
            },
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.image_corrupted),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        )
    }
}