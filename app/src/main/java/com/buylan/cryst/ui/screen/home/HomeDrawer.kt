package com.buylan.cryst.ui.screen.home

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.ui.model.DarkMode
import com.buylan.cryst.ui.Screen
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeDrawer(
    drawerState: DrawerState,
    darkMode: DarkMode,
    onStorageItemClick: (VirtualFile) -> Unit,
    onShowAbout: () -> Unit,
    onToggleDark: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    val scope = rememberCoroutineScope()
    val storageItemList = listOf(
        StorageItem(
            LocalFile(Environment.getRootDirectory()),
            painterResource(R.drawable.ic_memory),
            stringResource(R.string.root)
        ),
        StorageItem(
            LocalFile(Environment.getExternalStorageDirectory()),
            painterResource(R.drawable.ic_sd_card),
            stringResource(R.string.storage)
        )
    )

    ModalDrawerSheet(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth(0.8f),
        drawerShape = MaterialTheme.shapes.extraLarge.copy(
            topStart = CornerSize(0.dp),
            bottomStart = CornerSize(0.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
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
                        onClick = onToggleDark
                    ) {
                        Icon(
                            painter = painterResource(darkMode.icon),
                            contentDescription = null
                        )
                    }
                    Icon(
                        painter =  painterResource(R.drawable.ic_info),
                        contentDescription = null
                    )
                },
                onClick = onShowAbout
            )
            HorizontalDivider()

            Text(
                text = stringResource(R.string.local),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )

            storageItemList.forEach { item ->
                var fsUsage by remember { mutableFloatStateOf(0f) }

                LaunchedEffect(Unit) {
                    val progress = withContext(Dispatchers.IO) {
                        try {
                            val stat = StatFs(item.path.absolutePath)
                            val totalBytes = stat.totalBytes
                            val availableBytes = stat.availableBytes
                            if (totalBytes > 0) (totalBytes - availableBytes).toFloat() / totalBytes else 0f
                        } catch (_: Exception) {
                            0f
                        }
                    }
                    fsUsage = progress
                }

                DrawerStorageItem(
                    label = item.name ?: item.path.absolutePath,
                    icon = item.icon,
                    usage = fsUsage,
                    onClick = {
                        onStorageItemClick(item.path)
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

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
                    onNavigate(Screen.Apps)
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
                    onNavigate(Screen.Terminal)
                }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun DrawerStorageItem(
    label: String,
    icon: Painter,
    usage: Float,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label)
                    Text("${(usage * 100).toInt()}%")
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { usage },
                    drawStopIndicator = {},
                    gapSize = (-2).dp,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
        },
        selected = true,
        icon = {
            Icon(
                painter = icon,
                contentDescription = null
            )
        },
        onClick = onClick
    )
}

data class StorageItem(
    val path: VirtualFile,
    val icon: Painter,
    val name: String? = null
)