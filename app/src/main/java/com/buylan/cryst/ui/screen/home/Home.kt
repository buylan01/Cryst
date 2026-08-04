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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buylan.cryst.model.AppViewModel
import com.buylan.cryst.ui.screen.home.model.DialogsEvent
import com.buylan.cryst.ui.screen.home.model.HomeViewModel
import com.buylan.cryst.util.PanelPosition
import com.buylan.cryst.util.isRootPath
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.flow.SharedFlow
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
    val uiState by viewModel.uiState.collectAsState()
    val dialogsViewModel = viewModel.dialogsViewModel
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
            HomeDrawer(
                context = context,
                drawerState = drawerState,
                darkMode = appViewModel.darkMode,
                onShowAbout = { dialogsViewModel.show(DialogsEvent.AboutDialog) },
                onToggleDark = { appViewModel.toggleDarkMode() },
                onStorageItemClick = { viewModel.currentPanelViewModel.setPath(it) }
            )
        },
        drawerState = drawerState,
        gesturesEnabled = !drawerState.isClosed || drawerState.isAnimationRunning
    ) {
        Scaffold(
            topBar = {
                HomeTopAppBar(
                    currentPanel,
                    viewModel,
                    scope,
                    drawerState,
                    context
                )
            },
            bottomBar = {
                HomeBottomBar(viewModel, currentPanel)
            },
            contentWindowInsets = WindowInsets.displayCutout
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

                HomePanel(
                    modifier = Modifier.weight(1f),
                    panelState = leftPanelState,
                    panelViewModel = viewModel.leftPanelViewModel,
                    lazyState = leftLazyState,
                    panelPosition = PanelPosition.L,
                    viewModel = viewModel,
                    uiState = uiState
                )
                
                HomePanel(
                    modifier = Modifier.weight(1f),
                    panelState = rightPanelState,
                    panelViewModel = viewModel.rightPanelViewModel,
                    lazyState = rightLazyState,
                    panelPosition = PanelPosition.R,
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }

    fun handleRefresh(path: VirtualFile?) {
        path?.let {
            val refreshPosition: MutableList<PanelPosition> = mutableListOf()
            if (leftPanelState.path.absolutePath == path.absolutePath) refreshPosition += PanelPosition.L
            if (rightPanelState.path.absolutePath == path.absolutePath) refreshPosition += PanelPosition.R
            refreshPosition.forEach {
                viewModel.refreshPanel(it)
            }
        }
    }

    HomeDialogs(dialogsViewModel, viewModel, context, currentPanel, uiState) {
        handleRefresh(it)
    }
}