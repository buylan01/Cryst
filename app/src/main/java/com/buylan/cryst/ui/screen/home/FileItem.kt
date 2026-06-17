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

package com.buylan.cryst.ui.screen.home

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.buylan.cryst.R
import com.buylan.cryst.util.FileType
import com.buylan.cryst.util.formatFileDate
import com.buylan.cryst.util.formatFileSize
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItem(
    file: VirtualFile,
    type: FileType,
    highLight: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSwipe: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    Row(
        modifier = Modifier
            .background(
                color = if (!selected) Color.Transparent else MaterialTheme.colorScheme.inversePrimary
            )
            .combinedClickable(
                onClick = onClick, onLongClick = onLongClick
            )
            .fillMaxWidth()
            .padding(6.dp)
            .zIndex(-1f)
            .offset {
                IntOffset(offsetX.roundToInt(), 0)
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    offsetX = 0f
                    var dx = 0f
                    var dy = 0f
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        val deltaX = change.positionChange().x
                        val deltaY = change.positionChange().y
                        when {
                            abs(dy) > 20f && abs(dy) > abs(dx) -> {
                                break
                            }

                            abs(dx) > 20f && abs(dx) > abs(dy) -> {
                                change.consume()
                            }
                        }
                        dx += deltaX
                        dy += deltaY
                        offsetX += deltaX
                        offsetX = offsetX.coerceIn(
                            -120f, 120f
                        )
                    } while (event.changes.any { it.pressed })
                    val isHorizontalSwipe = abs(dx) > 60f && abs(dx) > abs(dy) * 2
                    if (isHorizontalSwipe) {
                        onSwipe()
                    }
                    offsetX = 0f
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current

        when(type) {
            FileType.IMAGE -> {
                val imageRequest = ImageRequest.Builder(context)
                    .data(File(file.path))
                    .size(64)
                    .crossfade(true)
                    .build()

                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Low,
                    loading = {
                        FileIcon(R.drawable.ic_image)
                    },
                    error = {
                        FileIcon(R.drawable.ic_broken_image)
                    }
                )
            }

            FileType.APK -> {
                var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                LaunchedEffect(file.path) {
                    withContext(Dispatchers.Default) {
                        val pm = context.packageManager
                        try {
                            pm.getPackageArchiveInfo(file.path, 0)
                        } catch (_: Exception) {
                            null
                        }?.let {
                            it.applicationInfo!!.apply {
                                sourceDir = file.absolutePath
                                publicSourceDir = file.absolutePath
                            }
                            bitmap = it.applicationInfo!!.loadIcon(pm).toBitmap()
                        }
                    }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    FileIcon(R.drawable.ic_apk_document)
                }
            }

            else -> {
                FileIcon(type.icon)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (!highLight) Color.Unspecified else MaterialTheme.colorScheme.primary
            )
            Row {
                Text(
                    text = formatFileDate(file, "yy-MM-dd HH:mm"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
                Spacer(Modifier.width(4.dp))
                if (!file.isDirectory) {
                    Text(
                        text = formatFileSize(file.length()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (Files.isSymbolicLink(Path(file.path))) {
            Icon(
                painter = painterResource(R.drawable.ic_link),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun FileIcon(iconRes: Int) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .border(
                width = 1.5.dp,
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            )
            .padding(4.dp)
    )
}

@Composable
fun UpwardItem(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_upward),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .border(
                        width = 1.5.dp,
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "..",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = " ",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}