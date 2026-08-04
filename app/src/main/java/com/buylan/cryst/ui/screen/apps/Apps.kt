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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.buylan.cryst.R
import com.buylan.cryst.ui.component.ApkDialogContent
import com.buylan.cryst.ui.component.AutoScrollBar
import com.buylan.cryst.ui.component.MenuType
import com.buylan.cryst.ui.screen.apps.model.ApkInfo
import com.buylan.cryst.ui.screen.apps.model.AppsViewModel


@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: AppsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val searchField = rememberTextFieldState()
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedDestination.ordinal,
        pageCount = { AppsDestination.entries.size }
    )
    val navigationBarHeight = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    fun getFilteredApps(
        apps: List<ApkInfo>
    ): List<ApkInfo> {
        val apps = if (!uiState.searchActive) {
            apps
        } else {
            val text = searchField.text
            apps.filter { app ->
                app.label
                    .contains(text, ignoreCase = true) || app.packageName.contains(text)
            }
        }
        return sortApps(apps, uiState.sortType)
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
                        onClick = onBack
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
        contentWindowInsets = WindowInsets.displayCutout
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
            fun ApksColumn(apps: List<ApkInfo>) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val lazyListState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = navigationBarHeight),
                        state = lazyListState
                    ) {
                        items(
                            getFilteredApps(apps),
                            key = { app -> app.packageName }
                        ) { app ->
                            AppItem(app) { viewModel.showAppDialog(app) }
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
                AlertDialog(
                    onDismissRequest = { viewModel.hideAppDialog() },
                    text = {
                        ApkDialogContent(
                            info = app,
                            menuType = MenuType.Installed
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.onExtract(app) }
                        ) {
                            Text(stringResource(R.string.extract))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.hideAppDialog() }
                        ) {
                            Text(stringResource(R.string.cancel))
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
    info: ApkInfo,
    onClick: (ApkInfo) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(info) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = info.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = info.label,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.padding(vertical = 2.dp))
                Text(
                    text = info.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun sortApps(apps: List<ApkInfo>, sortType: AppsSortType): List<ApkInfo> {
    return apps.sortedWith(
        when (sortType) {
            AppsSortType.LABEL -> compareBy { it.label.lowercase() }
            AppsSortType.SIZE -> compareBy { it.size }
            AppsSortType.UPDATE_TIME -> compareByDescending { it.lastUpdateTime }
            AppsSortType.INSTALL_TIME -> compareBy { it.firstInstallTime }
        }
    )
}

enum class AppsDestination(
    val label: Int
) {
    USER(R.string.user),
    SYSTEM(R.string.system),
}

enum class AppsSortType(
    val label: Int
) {
    LABEL(R.string.name),
    SIZE(R.string.size),
    INSTALL_TIME(R.string.install_time),
    UPDATE_TIME(R.string.update_time)
}