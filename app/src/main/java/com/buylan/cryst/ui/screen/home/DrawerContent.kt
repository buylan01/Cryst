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

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.activity.AppsActivity
import com.buylan.cryst.activity.TerminalActivity
import com.buylan.cryst.model.AppViewModel
import com.buylan.cryst.ui.screen.home.model.MainViewModel
import com.buylan.cryst.util.DefaultPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DrawerContent(
    context: Context,
    viewModel: MainViewModel,
    appViewModel: AppViewModel,
    drawerState: DrawerState,
    modifier: Modifier = Modifier
) {
    val isSystemInDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    ModalDrawerSheet(
        modifier = modifier.fillMaxWidth(0.8f),
        drawerShape = MaterialTheme.shapes.extraLarge.copy(
            topStart = CornerSize(0.dp),
            bottomStart = CornerSize(0.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            NavigationDrawerItem(
                label = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                selected = false,
                badge = {
                    IconButton(
                        onClick = {
                            appViewModel.toggleDarkMode(isSystemInDark)
                        }
                    ) {
                        Icon(
                            painter = painterResource(appViewModel.darkMode.icon),
                            contentDescription = null
                        )
                    }
                    Icon(
                        painter =  painterResource(R.drawable.ic_info),
                        contentDescription = null
                    )
                },
                onClick = {
                    viewModel.showAboutDialog = true
                }
            )
            HorizontalDivider()

            Text(
                stringResource(R.string.local),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            NavigationDrawerItem(
                label = {
                    var storageProgress by remember { mutableFloatStateOf(0f) }

                    LaunchedEffect(Unit) {
                        val progress = withContext(Dispatchers.IO) {
                            try {
                                val stat =
                                    StatFs(Environment.getExternalStorageDirectory().path)
                                val totalBytes = stat.totalBytes
                                val availableBytes = stat.availableBytes
                                if (totalBytes > 0) (totalBytes - availableBytes).toFloat() / totalBytes else 0f
                            } catch (_: Exception) {
                                0f
                            }
                        }
                        storageProgress = progress
                    }
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.storage))
                            Text("${(storageProgress * 100).toInt()}%")
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { storageProgress },
                            drawStopIndicator = {},
                            gapSize = (-2).dp,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }
                },
                selected = true,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_storage),
                        contentDescription = null
                    )
                },
                onClick = {
                    scope.launch {
                        viewModel.currentPanel.path = DefaultPath
                        drawerState.close()
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                stringResource(R.string.tools),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.apps)) },
                selected = false,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_apps),
                        contentDescription = null
                    )
                },
                onClick = {
                    context.startActivity(Intent(context, AppsActivity::class.java))
                }
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.terminal)) },
                selected = false,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_terminal_2),
                        contentDescription = null
                    )
                },
                onClick = {
                    context.startActivity(Intent(context, TerminalActivity::class.java))
                }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}