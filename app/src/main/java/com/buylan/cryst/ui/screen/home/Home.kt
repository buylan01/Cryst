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
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buylan.cryst.BuildConfig
import com.buylan.cryst.R
import com.buylan.cryst.activity.AppsActivity
import com.buylan.cryst.activity.LicensesActivity
import com.buylan.cryst.activity.SettingsActivity
import com.buylan.cryst.activity.TerminalActivity
import com.buylan.cryst.model.AppViewModel
import com.buylan.cryst.ui.component.AutoScrollBar
import com.buylan.cryst.ui.screen.home.dialog.ApkDialog
import com.buylan.cryst.ui.screen.home.dialog.AudioPlayer
import com.buylan.cryst.ui.screen.home.dialog.CompressDialog
import com.buylan.cryst.ui.screen.home.dialog.CopyDialog
import com.buylan.cryst.ui.screen.home.dialog.CreateDialog
import com.buylan.cryst.ui.screen.home.dialog.DeleteDialog
import com.buylan.cryst.ui.screen.home.dialog.MoveDialog
import com.buylan.cryst.ui.screen.home.dialog.OpenWithDialog
import com.buylan.cryst.ui.screen.home.dialog.PropertiesDialog
import com.buylan.cryst.ui.screen.home.dialog.RenameDialog
import com.buylan.cryst.ui.screen.home.dialog.ScriptDialog
import com.buylan.cryst.ui.screen.home.dialog.SearchDialog
import com.buylan.cryst.ui.screen.home.dialog.SortOrderDialog
import com.buylan.cryst.ui.screen.home.dialog.ToolDialog
import com.buylan.cryst.ui.screen.home.model.HomeViewModel
import com.buylan.cryst.ui.screen.home.model.PanelStates
import com.buylan.cryst.ui.screen.home.model.PanelViewModel
import com.buylan.cryst.util.DefaultPath
import com.buylan.cryst.util.FileType
import com.buylan.cryst.util.PanelPosition
import com.buylan.cryst.util.RootPath
import com.buylan.cryst.util.getFileType
import com.buylan.cryst.util.isRootPath
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun HomeScreen(
    context: Context,
    viewModel: HomeViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
    pathFlow: SharedFlow<String>
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogsState by viewModel.dialogsState.collectAsState()
    val leftPanelState by viewModel.leftPanelState.collectAsState()
    val rightPanelState by viewModel.rightPanelState.collectAsState()
    val currentPanel = if (uiState.panelPosition == PanelPosition.L) leftPanelState else rightPanelState
    val scope = rememberCoroutineScope()
    val leftLazyState = rememberLazyListState()
    val rightLazyState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BackHandler(
        enabled = !currentPanel.path.isRootPath() || currentPanel.selectedFiles.isNotEmpty()
    ) {
        viewModel.onNavigateBack()
    }

    BackHandler(
        enabled = !drawerState.isClosed
    ) {
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(
                context,
                viewModel,
                appViewModel,
                drawerState
            )
        },
        drawerState = drawerState,
        gesturesEnabled = !drawerState.isClosed || drawerState.isAnimationRunning
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentPanel.path.pathDisplay,
                                maxLines = 1,
                                overflow = TextOverflow.StartEllipsis,
                                softWrap = false,
                                modifier = Modifier.clickable(
                                    onClick = {
                                        viewModel.dialogsViewModel.showPathDialog()
                                    }
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = null
                            )
                        }
                    },
                    actions = {
                        Box {
                            var expanded by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = {
                                    expanded = true
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_more_vert),
                                    contentDescription = null
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.refresh)) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_refresh),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        viewModel.refreshPanel()
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.search)) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_search),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        viewModel.dialogsViewModel.showSearchDialog(currentPanel.path)
                                        expanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort)) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_sort),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        viewModel.dialogsViewModel.showSortDialog()
                                        expanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.settings)) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_settings),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                SettingsActivity::class.java
                                            )
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.exit)) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_exit_to_app),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = { (context as ComponentActivity).finishAffinity() }
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                enabled = viewModel.currentPanelViewModel.canUnNavigate,
                                onClick = { viewModel.currentPanelViewModel.unNavigate() }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_keyboard_arrow_left),
                                    contentDescription = null
                                )
                            }
                            IconButton(
                                enabled = viewModel.currentPanelViewModel.canReNavigate,
                                onClick = { viewModel.currentPanelViewModel.reNavigate() }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_keyboard_arrow_right),
                                    contentDescription = null,
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dialogsViewModel.showCreateDialog(currentPanel.path) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_add),
                                    contentDescription = null,
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.anotherPanelViewModel.setPath(currentPanel.path)
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_swap_horiz),
                                    contentDescription = null,
                                )
                            }
                            IconButton(onClick = { viewModel.onNavigateBack() }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_upward),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                )
            }
        ) { contentPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {

                LaunchedEffect(Unit) {
                    pathFlow.collect { path ->
                        viewModel.currentPanelViewModel.setPath(LocalFile(path))
                        drawerState.close()
                    }
                }

                @Composable
                fun Panel(
                    panelState: PanelStates,
                    panelViewModel: PanelViewModel,
                    lazyState: LazyListState,
                    panelPosition: PanelPosition
                ) {

                    LaunchedEffect(Unit) {
                        panelViewModel.scrollToIndex.collect { index ->
                            snapshotFlow { lazyState.layoutInfo.totalItemsCount }.first { it >= index }
                            lazyState.scrollToItem(index)
                        }
                    }

                    LaunchedEffect(panelState.path) {
                        viewModel.refreshPanel(panelPosition)
                    }

                    val backgroundColor by animateColorAsState(
                        targetValue = if (panelPosition == uiState.panelPosition) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLowest,
                        animationSpec = tween(150)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    drawRect(backgroundColor)
                                }
                                .pointerInput(panelPosition) {
                                    awaitEachGesture {
                                        awaitFirstDown(
                                            requireUnconsumed = false,
                                            pass = PointerEventPass.Initial
                                        )
                                        viewModel.setPanel(panelPosition)
                                    }
                                },
                            state = lazyState
                        ) {
                            item {
                                UpwardItem { panelViewModel.navigateBack() }
                            }
                            items(panelState.files, key = { it.hashCode() }) { file ->
                                FileItem(
                                    file = file,
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(durationMillis = 210, delayMillis = 10),
                                        fadeOutSpec = null
                                    ),
                                    type = getFileType(file),
                                    highLight = file.name in panelState.highLightFiles,
                                    selected = file.path in panelState.selectedFiles,
                                    onClick = {
                                        if (panelViewModel.selectionMode) {
                                            panelViewModel.toggleSelection(file)
                                        } else {
                                            viewModel.handleFileClick(context, file)
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.dialogsViewModel.showToolsDialog(
                                            panelViewModel.getSelectedFiles(file)
                                        )
                                    },
                                    onSwipe = {
                                        panelViewModel.swipeSelect(file)
                                    }
                                )
                            }
                        }
                        AutoScrollBar(
                            lazyState = lazyState,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }

                Panel(leftPanelState,viewModel.leftPanelViewModel, leftLazyState, PanelPosition.L)
                Panel(rightPanelState,viewModel.rightPanelViewModel, rightLazyState, PanelPosition.R)
            }
        }
    }

    fun handleRefresh(path: VirtualFile?) {
        path?.let {
            val refreshPosition =
                if (leftPanelState.path.absolutePath == path.absolutePath) PanelPosition.L
                else if (rightPanelState.path.absolutePath == path.absolutePath) PanelPosition.R
                else null
            refreshPosition?.let {
                viewModel.refreshPanel(it)
            }
        }
    }

    if (dialogsState.permissionRequest) {
        AlertDialog(
            onDismissRequest = { viewModel.dialogsViewModel.hidePermissionRequest() },
            title = { Text(stringResource(R.string.permission_request)) },
            text = {
                Text(stringResource(R.string.app_name) + stringResource(R.string.permission_manage_file_require))
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        context.startActivity(intent)
                        viewModel.dialogsViewModel.hidePermissionRequest()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        Toast.makeText(context, "TAT", Toast.LENGTH_SHORT).show()
                        viewModel.dialogsViewModel.hidePermissionRequest()
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (dialogsState.aboutDialog) {
        BasicAlertDialog(
            onDismissRequest = { viewModel.dialogsViewModel.hideAboutDialog() },
            content = {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.width(280.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(color = MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_folder),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.bodyLarge,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 160.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = BuildConfig.VERSION_NAME,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = {
                                val intent = Intent(context, LicensesActivity::class.java)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.open_source_libraries))
                        }
                    }
                }
            }
        )
    }

    if (dialogsState.pathDialog) {
        val textFieldState = rememberTextFieldState(initialText = currentPanel.path.absolutePath)
        AlertDialog(
            onDismissRequest = { viewModel.dialogsViewModel.hidePathDialog() },
            title = { Text(stringResource(R.string.go_to_path)) },
            text = {
                val focusRequester = remember { FocusRequester() }

                OutlinedTextField(
                    state = textFieldState,
                    label = { Text(stringResource(R.string.path)) },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.focusRequester(focusRequester)
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.currentPanelViewModel.setPath(LocalFile(textFieldState.text.toString()))
                        viewModel.dialogsViewModel.hidePathDialog()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dialogsViewModel.hidePathDialog() }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    dialogsState.openWithDialog?.let { file ->
        OpenWithDialog(
            onDismiss = { viewModel.dialogsViewModel.hideOpenWithDialog() }
        ) { viewModel.handleFileClick(context, file, it) }
    }

    dialogsState.toolsDialog?.let { files ->
        ToolDialog(
            files, uiState.panelPosition,
            onDismiss = { viewModel.dialogsViewModel.hideToolsDialog() },
            onToolAction = { viewModel.onToolAction(context,it, files) },
        )
    }

    dialogsState.deleteDialog?.let { files ->
        DeleteDialog(
            targetFiles = files,
            onDismiss = { viewModel.dialogsViewModel.hideDeleteDialog() },
            onRefresh = {
                handleRefresh(files.first().parentFile)
            }
        )
    }
    dialogsState.copyDialog?.let { state ->
        CopyDialog(
            source = state.files,
            target = state.path,
            onDismiss = { viewModel.dialogsViewModel.hideCopyDialog() },
            onRefresh = {
                handleRefresh(state.path)
            }
        )
    }
    dialogsState.moveDialog?.let { state ->
        MoveDialog(
            source = state.files,
            target = state.path,
            onDismiss = { viewModel.dialogsViewModel.hideMoveDialog() },
            onRefresh = {
                handleRefresh(state.files.first().parentFile)
                handleRefresh(state.path)
            }
        )
    }
    dialogsState.createDialog?.let { file ->
        CreateDialog(onDismiss = { viewModel.dialogsViewModel.hideCreateDialog() }, file) {
            handleRefresh(file)
        }
    }
    dialogsState.compressDialog?.let { files ->
        CompressDialog(files, { viewModel.dialogsViewModel.hideCompressDialog() })  {
            handleRefresh(files.first().parentFile)
        }
    }
    if (dialogsState.sortDialog) {
        SortOrderDialog(
            onDismiss = { viewModel.dialogsViewModel.hideSortDialog() },
            onSelect = { viewModel.currentPanelViewModel.setSort(it) },
            currentPanel,
            uiState.panelPosition
        )
    }
    dialogsState.searchDialog?.let { file ->
        SearchDialog(onDismiss = { viewModel.dialogsViewModel.hideSearchDialog() }, file) { file ->
            viewModel.currentPanelViewModel.setPath(file)
            viewModel.dialogsViewModel.hideSearchDialog()
        }
    }
    dialogsState.apkDialog?.let { file ->
        ApkDialog(
            context = context,
            targetFile = file as LocalFile,
            onDismiss = { viewModel.dialogsViewModel.hideApkDialog() },
            unpack = { viewModel.handleFileClick(context, file, type = FileType.ARCHIVE) })
    }
    dialogsState.audioDialog?.let { file ->
        AudioPlayer(onDismiss = { viewModel.dialogsViewModel.hideAudioDialog() }, file)
    }
    dialogsState.renameDialog?.let { file ->
        RenameDialog(file, { viewModel.dialogsViewModel.hideRenameDialog() }) {
            handleRefresh(file.parentFile)
        }
    }
    dialogsState.propertiesDialog?.let { files ->
        PropertiesDialog(files) { viewModel.dialogsViewModel.hidePropertiesDialog() }
    }
    dialogsState.runScriptDialog?.let { file ->
        ScriptDialog({ viewModel.dialogsViewModel.hideRunScriptDialog() }, file)
    }
}

@Composable
fun DrawerContent(
    context: Context,
    viewModel: HomeViewModel,
    appViewModel: AppViewModel,
    drawerState: DrawerState,
    modifier: Modifier = Modifier
) {
    val isSystemInDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    ModalDrawerSheet(
        modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(0.8f),
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
                    viewModel.dialogsViewModel.showAboutDialog()
                }
            )
            HorizontalDivider()

            Text(
                text = stringResource(R.string.local),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )

            NavigationDrawerItem(
                label = {
                    var usage by remember { mutableFloatStateOf(0f) }

                    LaunchedEffect(Unit) {
                        val progress = withContext(Dispatchers.IO) {
                            try {
                                val stat = StatFs(Environment.getRootDirectory().path)
                                val totalBytes = stat.totalBytes
                                val availableBytes = stat.availableBytes
                                if (totalBytes > 0) (totalBytes - availableBytes).toFloat() / totalBytes else 0f
                            } catch (_: Exception) {
                                0f
                            }
                        }
                        usage = progress
                    }
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.root))
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
                        painter = painterResource(R.drawable.ic_storage),
                        contentDescription = null
                    )
                },
                onClick = {
                    scope.launch {
                        viewModel.currentPanelViewModel.setPath(RootPath)
                        drawerState.close()
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

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
                        viewModel.currentPanelViewModel.setPath(DefaultPath)
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
