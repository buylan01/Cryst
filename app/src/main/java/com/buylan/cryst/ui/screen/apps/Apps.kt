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

package com.buylan.cryst.ui.screen.apps

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.buylan.cryst.R
import com.buylan.cryst.ui.component.ApkInfoColumn
import com.buylan.cryst.ui.component.AutoScrollBar
import java.io.File


@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(){
    val context = LocalContext.current
    val viewModel: AppsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val searchField = rememberTextFieldState()
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedDestination.ordinal,
        pageCount = { AppsDestination.entries.size }
    )
    val pm = context.packageManager

    fun getFilteredApps(
        apps: List<PackageInfo>
    ): List<PackageInfo> {
        val apps = if (!uiState.searchActive) {
            apps
        } else {
            val text = searchField.text
            apps.filter { app ->
                app.applicationInfo!!.loadLabel(pm)
                    .toString()
                    .contains(text, ignoreCase = true) || app.packageName.contains(text)
            }
        }
        return sortApps(apps, uiState.sortType, pm)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!uiState.searchActive) {
                        Text(stringResource(R.string.apps))
                    } else {
                        val focusRequester = remember { FocusRequester() }
                        val colors = TextFieldDefaults.colors()
                        BasicTextField(
                            state = searchField,
                            lineLimits = TextFieldLineLimits.SingleLine,
                            textStyle = MaterialTheme.typography.titleMedium.merge(TextStyle(color = colors.focusedTextColor)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            cursorBrush = SolidColor(colors.cursorColor)
                        )

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { (context as ComponentActivity).finish() }
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
                            val text = searchField.text
                            if (text.isEmpty()) {
                                viewModel.setSearchActive(!uiState.searchActive)
                            } else {
                                searchField.clearText()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(if (uiState.searchActive) R.drawable.ic_close else R.drawable.ic_search),
                            contentDescription = null
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.showMenu()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = uiState.showMenu,
                        onDismissRequest = { viewModel.hideMenu() }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort)) },
                            onClick = {
                                viewModel.showSortDialog()
                                viewModel.hideMenu()
                            }
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0,0,0,0)
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            PrimaryTabRow(
                selectedTabIndex = uiState.selectedDestination.ordinal
            ) {
                AppsDestination.entries.forEachIndexed { index, destination ->
                    Tab(
                        selected = uiState.selectedDestination == AppsDestination.entries[index],
                        onClick = {
                            viewModel.setDestination(AppsDestination.entries[index])
                        },
                        text = {
                            Text(
                                text = stringResource(destination.label),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            @Composable
            fun ApksColumn(apps: List<PackageInfo>) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val lazyListState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState
                    ) {
                        items(
                            getFilteredApps(apps),
                            key = { app -> app.packageName }
                        ) { app ->
                            AppItem(app, pm) { viewModel.showAppDialog(app) }
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                    AutoScrollBar(
                        lazyState = lazyListState,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }

            LaunchedEffect(uiState.selectedDestination) {
                pagerState.animateScrollToPage(uiState.selectedDestination.ordinal)
            }

            LaunchedEffect(pagerState.currentPage) {
                viewModel.setDestination(AppsDestination.entries[pagerState.currentPage])
            }

            if (!uiState.isLoading) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (AppsDestination.entries[page]) {
                        AppsDestination.USER -> {
                            ApksColumn(getFilteredApps(uiState.userApps))
                        }

                        AppsDestination.SYSTEM -> {
                            ApksColumn(getFilteredApps(uiState.systemApps))
                        }
                    }
                }
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { CircularProgressIndicator() }
            }

            uiState.appDialog?.let { app ->
                val appName = pm.getApplicationLabel(app.applicationInfo!!).toString()
                AlertDialog(
                    onDismissRequest = { viewModel.hideAppDialog() },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val appIcon = remember(app.packageName) {
                                    try {
                                        pm.getApplicationIcon(app.packageName)
                                    } catch (_: Exception) {
                                        null
                                    }
                                }

                                appIcon?.let { icon ->
                                    AsyncImage(
                                        model = icon,
                                        contentDescription = "App icon",
                                        modifier = Modifier.size(48.dp),
                                        contentScale = ContentScale.Fit,
                                        placeholder = ColorPainter(Color.LightGray)
                                    )
                                } ?: Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.LightGray)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = appName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = app.versionName.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            ApkInfoColumn(app, true,null)
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.onExtract(app)
                            }
                        ) {
                            Text(stringResource(R.string.extract))
                        }
                    },
                    dismissButton = {
                        Row {
                            IconButton(
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_DELETE)
                                        .apply {
                                            data = Uri.fromParts("package", app.packageName, null)
                                        }
                                    )
                                }
                            ) {
                                Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = null)
                            }
                            IconButton(
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .apply {
                                            data = Uri.fromParts("package", app.packageName, null)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                    )
                                }
                            ) {
                                Icon(painter = painterResource(R.drawable.ic_info), contentDescription = null)
                            }
                            IconButton(
                                onClick = {
                                    try {
                                        context.startActivity(pm.getLaunchIntentForPackage(app.packageName))
                                    } catch (_: NullPointerException) {
                                        Toast.makeText(context, "应用没有主活动", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(painter = painterResource(R.drawable.ic_open_in_new), contentDescription = null)
                            }
                        }
                    }
                )
            }
        }
        uiState.locateDialog?.let { path ->
            AlertDialog(
                onDismissRequest = {
                    viewModel.hideLocateDialog()
                },

                title = {
                    Text(stringResource(R.string.tip))
                },

                text = {
                    Text(stringResource(R.string.file_saved_to, path))
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onLocate(path)
                            viewModel.hideLocateDialog()
                        }
                    ) {
                        Text(stringResource(R.string.locate))
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.hideLocateDialog()
                        }
                    ) {
                        Text(stringResource(R.string.dismiss))
                    }
                }
            )
        }
        if (uiState.showSortDialog) {
            var selectedSortOption by remember { mutableStateOf(uiState.sortType) }
            AlertDialog(
                onDismissRequest = { viewModel.hideSortDialog() },
                title = { Text(text = stringResource(R.string.sort)) },
                text = {
                    Column(
                        modifier = Modifier
                    ) {
                        AppsSortType.entries.forEach { sortType ->
                            ListItem(
                                headlineContent = { Text(stringResource(sortType.label)) },
                                leadingContent = {
                                    RadioButton(
                                        selected = selectedSortOption == sortType,
                                        onClick = { selectedSortOption = sortType }
                                    )
                                },
                                modifier = Modifier
                                    .clickable { selectedSortOption = sortType },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.setSortType(selectedSortOption)
                            viewModel.hideSortDialog()
                        }
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.hideSortDialog() }
                    ) {
                        Text(stringResource(R.string.dismiss))
                    }
                }
            )
        }
    }
}


@Composable
fun AppItem(
    app: PackageInfo,
    packageManager: PackageManager,
    onClick: (PackageInfo) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(app) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val px = LocalDensity.current.run { 48.dp.toPx().toInt() }
            val appIcon = remember(app.packageName, px) {
                try {
                    val drawable = packageManager.getApplicationIcon(app.packageName)
                    drawable.toBitmap(
                        width = px,
                        height = px
                    ).asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            }

            appIcon?.let { icon ->
                Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = packageManager.getApplicationLabel(app.applicationInfo!!).toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.padding(vertical = 2.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun sortApps(apps: List<PackageInfo>, sortType: AppsSortType, pm: PackageManager): List<PackageInfo> {
    return apps.sortedWith(
        when (sortType) {
            AppsSortType.LABEL -> compareBy { it.applicationInfo!!.loadLabel(pm).toString().lowercase() }
            AppsSortType.SIZE -> compareBy { File(it.applicationInfo!!.sourceDir).length() }
            AppsSortType.UPDATE_TIME -> compareByDescending { it.lastUpdateTime }
            AppsSortType.INSTALL_TIME -> compareBy { it.firstInstallTime }
        }
    )
}

enum class AppsDestination(
    val label: Int
) {
    USER(R.string.user),
    SYSTEM(R.string.follow_system),
}

enum class AppsSortType(
    val label: Int
) {
    LABEL(R.string.name),
    SIZE(R.string.size),
    INSTALL_TIME(R.string.install_time),
    UPDATE_TIME(R.string.update_time)
}