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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buylan.cryst.activity.BytesEditorActivity
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val _dialogsViewModel: DialogsViewModel = DialogsViewModel(),
    private val _leftPanelViewModel: PanelViewModel = PanelViewModel(),
    private val _rightPanelViewModel: PanelViewModel = PanelViewModel()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val dialogsViewModel: DialogsViewModel get() = _dialogsViewModel
    val dialogsState: StateFlow<DialogsState> = _dialogsViewModel.dialogsState

    val leftPanelViewModel: PanelViewModel get() = _leftPanelViewModel
    val leftPanelState: StateFlow<PanelStates> = _leftPanelViewModel.panelStates

    val rightPanelViewModel: PanelViewModel get() = _rightPanelViewModel
    val rightPanelState: StateFlow<PanelStates> = _rightPanelViewModel.panelStates

    fun setPanel(panelPosition: PanelPosition) {
        _uiState.update {
            it.copy(panelPosition = panelPosition)
        }
    }

    val anotherPath: VirtualFile
        get() =  if (_uiState.value.panelPosition == PanelPosition.R) leftPanelState.value.path else rightPanelState.value.path

    val currentPanelViewModel: PanelViewModel
        get() = if (_uiState.value.panelPosition == PanelPosition.L) _leftPanelViewModel else _rightPanelViewModel
    val anotherPanelViewModel: PanelViewModel
        get() = if (_uiState.value.panelPosition == PanelPosition.R) _leftPanelViewModel else _rightPanelViewModel

    fun onNavigateBack() = currentPanelViewModel.navigateBack()

    fun refreshPanel(panelPosition: PanelPosition = _uiState.value.panelPosition) {
        val panel = if (panelPosition == PanelPosition.L) _leftPanelViewModel else _rightPanelViewModel
        viewModelScope.launch {
            panel.refresh { path, sortType ->
                withContext(Dispatchers.IO) {
                    accessFiles(path, sortType)
                }
            }
        }
    }

    fun handleFileClick(context: Context, file: VirtualFile, type: FileType? = null) {
        if (file.isDirectory) {
            currentPanelViewModel.setPath(file)
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
                        currentPanelViewModel.setPath(ArchiveFile(entranceFile = actualFile))
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                FileType.SCRIPT -> _dialogsViewModel.showRunScriptDialog(file)
                FileType.BYTES -> context.startActivity(
                    Intent(context, BytesEditorActivity::class.java)
                        .putExtra(BytesEditorActivity.EXTRA_FILE_PATH, actualFile.path)
                )
                else -> _dialogsViewModel.showOpenWithDialog(actualFile)
            }
        }
    }

    fun onToolAction(context: Context, action: ToolAction, file: List<VirtualFile>) {
        _dialogsViewModel.hideToolsDialog()

        when (action) {
            ToolAction.Move -> {
                _dialogsViewModel.showMoveDialog(ExtraDialogState(file, anotherPath))
            }
            ToolAction.Copy -> {
                _dialogsViewModel.showCopyDialog(ExtraDialogState(file, anotherPath))
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