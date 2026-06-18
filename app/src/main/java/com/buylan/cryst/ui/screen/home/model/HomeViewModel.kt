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

package com.buylan.cryst.ui.screen.home.model

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buylan.cryst.activity.FontActivity
import com.buylan.cryst.activity.ImageActivity
import com.buylan.cryst.activity.TextEditorActivity
import com.buylan.cryst.activity.VideoActivity
import com.buylan.cryst.util.FileType
import com.buylan.cryst.util.PanelPosition
import com.buylan.cryst.util.ToolAction
import com.buylan.cryst.util.accessFiles
import com.buylan.cryst.util.getActualFile
import com.buylan.cryst.util.getFileType
import com.buylan.cryst.util.shareFile
import com.buylan.cryst.vfs.ArchiveFile
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val _dialogsViewModel: DialogsViewModel = DialogsViewModel()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val dialogsViewModel: DialogsViewModel = _dialogsViewModel
    val dialogsState: StateFlow<DialogsState> = _dialogsViewModel.dialogsState

    var panelPosition: PanelPosition by mutableStateOf(PanelPosition.L)
        private set

    fun setPanel(panel: PanelPosition) {
        panelPosition = panel
    }

    private val _scrollToIndex = MutableSharedFlow<Int>(extraBufferCapacity = 1)

    val scrollToIndex = _scrollToIndex.asSharedFlow()
    val leftPanelState = PanelStates()
    val rightPanelState = PanelStates()
    val currentPanel
        get() =  if (panelPosition == PanelPosition.L) leftPanelState else rightPanelState
    val anotherPanel
        get() =  if (panelPosition == PanelPosition.R) leftPanelState else rightPanelState
    val currentPath
        get() = currentPanel.path

    fun onNavigateBack() = currentPanel.navigateBack()
    fun refreshPanel(panelState: PanelStates) {
        viewModelScope.launch {
            panelState.files = withContext(Dispatchers.IO) {
                if (!panelState.path.isDirectory) {
                    panelState.highLightFiles = setOf(panelState.path.name)
                    panelState.path = panelState.path.parentFile!!
                }
                accessFiles(panelState.path, panelState.sortType)
            }

            if (panelState.highLightFiles.isNotEmpty()) {
                val index = panelState.files.indexOfFirst { file ->
                    file.name.equals(
                        panelState.highLightFiles.first(),
                        ignoreCase = true
                    )
                }
                if (index != -1) {
                    _scrollToIndex.tryEmit(index)
                }
            }

            panelState.resetSelection()
        }
    }

    fun handleFileClick(context: Context, file: VirtualFile, type: FileType? = null) {
        if (file.isDirectory) {
            currentPanel.path = file
            currentPanel.resetHighLight()
        } else {
            val actualType = type ?: getFileType(file)
            val actualFile = getActualFile(context, file)

            when (actualType) {
                FileType.TEXT -> context.startActivity(
                    Intent(context, TextEditorActivity::class.java)
                        .putExtra("filePath", actualFile.path)
                )
                FileType.IMAGE -> context.startActivity(
                    Intent(context, ImageActivity::class.java)
                        .putExtra("filePath", actualFile.path)
                )
                FileType.FONT -> context.startActivity(
                    Intent(context, FontActivity::class.java)
                        .putExtra("filePath", actualFile.path)
                )
                FileType.VIDEO -> context.startActivity(
                    Intent(context, VideoActivity::class.java)
                        .putExtra("filePath", actualFile.path)
                )
                FileType.APK -> _dialogsViewModel.showApkDialog(actualFile)
                FileType.AUDIO -> _dialogsViewModel.showAudioDialog(actualFile)
                FileType.ARCHIVE -> {
                    try {
                        currentPanel.path = ArchiveFile(entranceFile = actualFile)
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                else -> _dialogsViewModel.showOpenWithDialog(actualFile)
            }
        }
    }

    fun onToolAction(context: Context, action: ToolAction, file: List<VirtualFile>) {
        _dialogsViewModel.hideToolsDialog()

        when (action) {
            ToolAction.Move -> {
                _dialogsViewModel.showMoveDialog(ExtraDialogState(file, anotherPanel.path))
            }
            ToolAction.Copy -> {
                _dialogsViewModel.showCopyDialog(ExtraDialogState(file, anotherPanel.path))
            }
            ToolAction.Rename -> {
                _dialogsViewModel.showRenameDialog(file.singleOrNull() ?: return)
            }
            ToolAction.Delete -> {
                _dialogsViewModel.showDeleteDialog(file)
            }
            ToolAction.Properties -> {
                _dialogsViewModel.showPropertiesDialog(file)
            }
            ToolAction.OpenWith -> {
                _dialogsViewModel.showOpenWithDialog(file.singleOrNull() ?: return)
            }
            ToolAction.Share -> {
                context.shareFile(file.singleOrNull() ?: return)
            }
            ToolAction.Compress -> {
                _dialogsViewModel.showCompressDialog(file)
            }
        }
    }
}