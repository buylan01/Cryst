package com.buylan.cryst.ui.screen.image

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.buylan.cryst.R
import com.jvziyaoyao.scale.zoomable.zoomable.ZoomableView
import com.jvziyaoyao.scale.zoomable.zoomable.rememberZoomableState
import java.io.File

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewer(
    context: Context,
    filePath: String
) {
    val file = File(filePath)

    val backgroundColors = listOf(
        Color.Black,
        Color.Gray,
        Color.White
    )

    var currentBgColorIndex by remember { mutableIntStateOf(0) }
    val currentBgColor = backgroundColors[currentBgColorIndex]
    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = file.name,
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
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            var loaded by remember { mutableStateOf(false) }
            var imageSize by remember { mutableStateOf(Size(100f, 100f)) }
            val builder = ImageRequest.Builder(LocalContext.current)
                .data(file)
                .size(coil.size.Size.ORIGINAL)
                .listener(
                    onSuccess = { _, result ->
                        imageSize = Size(
                            width = result.drawable.intrinsicWidth.toFloat(),
                            height = result.drawable.intrinsicHeight.toFloat()
                        )
                        loaded = true
                    }
                )
                .build()
            val state = rememberZoomableState(contentSize = imageSize)
            ZoomableView(state = state) {
                AsyncImage(
                    model = builder,
                    contentDescription = file.name,
                    contentScale = ContentScale.Fit,
                    filterQuality = FilterQuality.High,
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (!loaded) CircularProgressIndicator()
        }
    }
}