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
import android.provider.Settings
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buylan.cryst.BuildConfig
import com.buylan.cryst.R
import com.buylan.cryst.activity.LicensesActivity
import com.buylan.cryst.activity.SettingsActivity
import com.buylan.cryst.model.AppViewModel
import com.buylan.cryst.ui.component.materialScrollbarStyle
import com.buylan.cryst.ui.screen.home.dialog.AudioPlayer
import com.buylan.cryst.ui.screen.home.dialog.CompressDialog
import com.buylan.cryst.ui.screen.home.dialog.CopyDialog
import com.buylan.cryst.ui.screen.home.dialog.CreateDialog
import com.buylan.cryst.ui.screen.home.dialog.DeleteDialog
import com.buylan.cryst.ui.screen.home.dialog.MoveDialog
import com.buylan.cryst.ui.screen.home.dialog.OpenWithDialog
import com.buylan.cryst.ui.screen.home.dialog.PackageDetail
import com.buylan.cryst.ui.screen.home.dialog.PropertiesDialog
import com.buylan.cryst.ui.screen.home.dialog.RenameDialog
import com.buylan.cryst.ui.screen.home.dialog.SearchDialog
import com.buylan.cryst.ui.screen.home.dialog.SortOrderDialog
import com.buylan.cryst.ui.screen.home.dialog.ToolDialog
import com.buylan.cryst.ui.screen.home.model.HomeViewModel
import com.buylan.cryst.ui.screen.home.model.PanelStates
import com.buylan.cryst.util.FileType
import com.buylan.cryst.util.PanelPosition
import com.buylan.cryst.util.getFileType
import com.buylan.cryst.util.isRootPath
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile
import io.github.oikvpqya.compose.fastscroller.VerticalScrollbar
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    val homeUiState by viewModel.uiState.collectAsState()
    val dialogsState by viewModel.dialogsState.collectAsState()
    val scope = rememberCoroutineScope()
    val leftLazyState = rememberLazyListState()
    val rightLazyState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BackHandler(
        enabled = !viewModel.currentPanel.path.isRootPath() || viewModel.currentPanel.selectedFiles.isNotEmpty()
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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = viewModel.currentPath.pathDisplay,
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
                                        viewModel.refreshPanel(viewModel.currentPanel)
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
                                        viewModel.dialogsViewModel.showSearchDialog(viewModel.currentPath)
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
                                enabled = viewModel.currentPanel.canUnNavigate,
                                onClick = { viewModel.currentPanel.unNavigate() }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_keyboard_arrow_left),
                                    contentDescription = null
                                )
                            }
                            IconButton(
                                enabled = viewModel.currentPanel.canReNavigate,
                                onClick = { viewModel.currentPanel.reNavigate() }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_keyboard_arrow_right),
                                    contentDescription = null,
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dialogsViewModel.showCreateDialog(viewModel.currentPath) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_add),
                                    contentDescription = null,
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.anotherPanel.path = viewModel.currentPanel.path
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

                LaunchedEffect(viewModel.leftPanelState.path) {
                    viewModel.refreshPanel(viewModel.leftPanelState)
                }

                LaunchedEffect(viewModel.rightPanelState.path) {
                    viewModel.refreshPanel(viewModel.rightPanelState)
                }

                LaunchedEffect(Unit) {
                    viewModel.scrollToIndex.collect { index ->
                        val currentState = if (viewModel.panelPosition == PanelPosition.L) leftLazyState else rightLazyState

                        snapshotFlow { currentState.layoutInfo.totalItemsCount }.first { it > index }
                        currentState.scrollToItem(index)
                    }
                }

                LaunchedEffect(Unit) {
                    pathFlow.collect { path ->
                        viewModel.currentPanel.path = LocalFile(path)
                        drawerState.close()
                    }
                }

                fun handleFileLongClick(file: List<VirtualFile>) {
                    viewModel.dialogsViewModel.showToolsDialog(file)
                }

                val animation = (fadeIn(animationSpec = tween(220,0)) + scaleIn(
                    initialScale = 0.99f, animationSpec = tween(220,0)
                )).togetherWith(fadeOut(animationSpec = tween(0,0)))

                @Composable
                fun Panel(
                    panelState: PanelStates,
                    lazyState: LazyListState,
                    panelPosition: PanelPosition
                ) {
                    val backgroundColor by animateColorAsState(
                        targetValue = if (panelPosition == viewModel.panelPosition) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLowest,
                        animationSpec = tween(150)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    ) {
                        AnimatedContent(
                            targetState = panelState.files,
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = { animation }
                        ) { files ->
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = backgroundColor)
                                    .pointerInteropFilter { event ->
                                        if (event.action == MotionEvent.ACTION_DOWN) {
                                            viewModel.setPanel(panelPosition)
                                        }
                                        false
                                    },
                                state = lazyState
                            ) {
                                item {
                                    UpwardItem { panelState.navigateBack() }
                                }
                                items(files, key = { it.hashCode() }) { file ->
                                    FileItem(
                                        file = file,
                                        type = getFileType(file),
                                        highLight = file.name in panelState.highLightFiles,
                                        selected = file.path in panelState.selectedFiles,
                                        onClick = {
                                            if (panelState.selectionMode) {
                                                panelState.toggleSelection(file)
                                            } else {
                                                viewModel.handleFileClick(context, file)
                                            }
                                        },
                                        onLongClick = {
                                            val selected =
                                                panelState.files.filter { it.path in panelState.selectedFiles }
                                            handleFileLongClick(selected.ifEmpty { listOf(file) })
                                        },
                                        onSwipe = {
                                            panelState.swipeSelect(file)
                                        }
                                    )
                                }
                            }
                        }
                        var showScrollbar by remember { mutableStateOf(false) }
                        LaunchedEffect(lazyState) {
                            snapshotFlow {
                                val layoutInfo = lazyState.layoutInfo
                                val vh = layoutInfo.viewportSize.height
                                if (vh <= 0) false
                                else {
                                    val vis = layoutInfo.visibleItemsInfo
                                    if (vis.isEmpty()) false
                                    else {
                                        val avg = vis.sumOf { it.size } / vis.size
                                        val totalEst = avg * layoutInfo.totalItemsCount
                                        totalEst > 2 * vh
                                    }
                                }
                            }.distinctUntilChanged()
                                .collect { showScrollbar = it }
                        }
                        if (showScrollbar)
                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(lazyState),
                            style = materialScrollbarStyle(),
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }

                Panel(viewModel.leftPanelState, leftLazyState, PanelPosition.L)
                Panel(viewModel.rightPanelState, rightLazyState, PanelPosition.R)
            }
        }
    }

    fun handleRefresh(path: VirtualFile?, highlights: Set<String> = emptySet()) {
        path?.let {
            val panelState =
                if (viewModel.leftPanelState.path.absolutePath == path.absolutePath) viewModel.leftPanelState
                else if (viewModel.rightPanelState.path.absolutePath == path.absolutePath) viewModel.rightPanelState
                else null
            panelState?.let {
                it.highLightFiles = highlights
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
        val textFieldState = rememberTextFieldState(initialText = viewModel.currentPanel.path.absolutePath)
        AlertDialog(
            onDismissRequest = { viewModel.dialogsViewModel.hidePathDialog() },
            title = { Text(stringResource(R.string.path)) },
            text = {
                val focusRequester = remember { FocusRequester() }

                TextField(
                    state = textFieldState,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.focusRequester(focusRequester)
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.currentPanel.path = LocalFile(textFieldState.text.toString())
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
            files, viewModel.panelPosition,
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
            viewModel.currentPanel,
            viewModel.panelPosition
        )
    }
    dialogsState.searchDialog?.let { file ->
        SearchDialog(onDismiss = { viewModel.dialogsViewModel.hideSearchDialog() }, file) { file ->
            viewModel.currentPanel.path = file
            viewModel.dialogsViewModel.hideSearchDialog()
        }
    }
    dialogsState.apkDialog?.let { file ->
        PackageDetail(
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
}

