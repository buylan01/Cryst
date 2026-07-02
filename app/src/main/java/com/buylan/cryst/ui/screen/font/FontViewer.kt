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

package com.buylan.cryst.ui.screen.font

import android.content.Context
import android.graphics.Typeface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buylan.cryst.R
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontViewer(
    context: Context,
    filePath: String
) {
    val file = File(filePath)

    var fontFamily by remember { mutableStateOf<FontFamily?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(filePath) {
        val typeface = try {
            Typeface.createFromFile(File(filePath))
        } catch (_: Exception) {
            null
        }

        if (typeface != null) {
            fontFamily = FontFamily(typeface)
            isLoading = false
        } else {
            error = "无法加载字体"
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(file.name, maxLines = 1, softWrap = false, overflow = TextOverflow.StartEllipsis) },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as ComponentActivity).finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {  }) {
                        Icon(painter = painterResource(R.drawable.ic_edit), null)
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.displayCutout
    ) { contentPadding ->
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                error != null -> {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    val fontFamily = fontFamily
                    val textStyles = listOf(
                        TextStyle(fontFamily = fontFamily, fontSize = 12.sp),
                        TextStyle(fontFamily = fontFamily),
                        TextStyle(fontFamily = fontFamily, fontSize = 16.sp),
                        TextStyle(fontFamily = fontFamily, fontSize = 20.sp),
                        TextStyle(fontFamily = fontFamily, fontSize = 24.sp),
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.font_sample_letter),
                                style = TextStyle(fontFamily = fontFamily, fontSize = 16.sp)
                            )
                        }

                        item {
                            Text(
                                text = stringResource(R.string.font_sample_symbols),
                                style = TextStyle(fontFamily = fontFamily, fontSize = 16.sp)
                            )
                        }

                        item {
                            Column {
                                textStyles.forEach { style ->
                                    Text(
                                        text = stringResource(R.string.font_sample_text),
                                        style = style,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 2.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}