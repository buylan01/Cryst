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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Unarchive
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.activity.AppListActivity
import com.buylan.cryst.activity.TerminalActivity
import com.buylan.cryst.model.AppViewModel
import com.buylan.cryst.model.DarkMode
import com.buylan.cryst.ui.screen.home.model.MainViewModel
import com.buylan.cryst.util.RootPath
import com.buylan.cryst.vfs.LocalFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.io.path.Path

@Composable
fun ModalDrawer(
    context: Context,
    viewModel: MainViewModel,
    appViewModel: AppViewModel,
    drawerState: DrawerState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val isSystemInDark = isSystemInDarkTheme()
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
                            appViewModel.apply {
                                when (darkMode) {
                                    DarkMode.System -> {
                                        if (isSystemInDark) {
                                            setDarkTheme(DarkMode.Light)
                                        } else {
                                            setDarkTheme(DarkMode.Dark)
                                        }
                                    }

                                    DarkMode.Light -> {
                                        setDarkTheme(DarkMode.Dark)
                                    }

                                    DarkMode.Dark -> {
                                        setDarkTheme(DarkMode.Light)
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = when(appViewModel.darkMode) {
                                DarkMode.System -> Icons.Default.BrightnessAuto
                                DarkMode.Light  -> Icons.Default.LightMode
                                DarkMode.Dark   -> Icons.Default.DarkMode
                            },
                            contentDescription = null
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Info,
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
                        imageVector = Icons.Default.Storage,
                        contentDescription = null
                    )
                },
                onClick = {
                    scope.launch {
                        viewModel.currentPanelState().path = LocalFile(RootPath)
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
                        imageVector = Icons.Default.Unarchive,
                        contentDescription = null
                    )
                },
                onClick = {
                    context.startActivity(Intent(context, AppListActivity::class.java))
                }
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.terminal)) },
                selected = false,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Terminal,
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