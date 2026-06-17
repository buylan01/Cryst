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
import com.buylan.cryst.R
import com.buylan.cryst.activity.FontActivity
import com.buylan.cryst.activity.ImageActivity
import com.buylan.cryst.activity.TextEditorActivity
import com.buylan.cryst.activity.VideoActivity
import com.buylan.cryst.ui.screen.home.HomeUiState
import com.buylan.cryst.ui.screen.home.PanelStates
import com.buylan.cryst.ui.screen.home.dialog.DialogsState
import com.buylan.cryst.ui.screen.home.dialog.DialogsViewModel
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val _dialogsViewModel: DialogsViewModel = DialogsViewModel()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val dialogsViewModel: DialogsViewModel = _dialogsViewModel
    val dialogsState: StateFlow<DialogsState> = _dialogsViewModel.dialogsState

    fun setPanel(panel: PanelPosition) {
        _uiState.update { currentState ->
            currentState.copy(panelPosition = panel)
        }
    }

    private val _scrollToIndex = MutableSharedFlow<Int>(extraBufferCapacity = 1)

    val scrollToIndex = _scrollToIndex.asSharedFlow()
    val leftPanelState = PanelStates()
    val rightPanelState = PanelStates()
    val currentPanel
        get() =  if (_uiState.value.panelPosition == PanelPosition.L) leftPanelState else rightPanelState
    val anotherPanel
        get() =  if (_uiState.value.panelPosition == PanelPosition.R) leftPanelState else rightPanelState
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
                _dialogsViewModel.showDeleteDialog(OperationDialogState(file))
            }
            ToolAction.Properties -> {
                _dialogsViewModel.showPropertiesDialog(OperationDialogState(file))
            }
            ToolAction.OpenWith -> {
                _dialogsViewModel.showOpenWithDialog(file.singleOrNull() ?: return)
            }
            ToolAction.Share -> {
                context.shareFile(file.singleOrNull() ?: return)
            }
            ToolAction.Compress -> {
                _dialogsViewModel.showCompressDialog(OperationDialogState(file))
            }
        }
    }
}

enum class ToolAction { Move, Copy, Rename, Delete, Properties, OpenWith, Compress, Share }
enum class SortType(
    val label: Int
) {
    NAME(R.string.name), SIZE(R.string.size), TIME(R.string.time), TYPE(R.string.type)
}

enum class PanelPosition { L, R }
data class OperationDialogState(
    val files: List<VirtualFile>
)
data class ExtraDialogState(
    val files: List<VirtualFile>,
    val path: VirtualFile
)
enum class FileType(
    val label: Int,
    val icon: Int
) {
    FOLDER(R.string.folder, R.drawable.ic_folder),
    FILE(R.string.file, R.drawable.ic_draft),
    TEXT(R.string.text, R.drawable.ic_description),
    AUDIO(R.string.audio, R.drawable.ic_audio_file),
    IMAGE(R.string.image, R.drawable.ic_image),
    VIDEO(R.string.video, R.drawable.ic_video_file),
    ARCHIVE(R.string.archive, R.drawable.ic_folder_zip),
    APK(R.string.installable, R.drawable.ic_apk_document),
    SCRIPT(R.string.script, R.drawable.ic_terminal_2),
    FONT(R.string.font, R.drawable.ic_font_download)
}

sealed class FileOperaUiState {
    object Idle : FileOperaUiState()
    object InProgress : FileOperaUiState()
    data class Progress(val current: Int, val total: Int, val percentage: Int, val failed: Int) :
        FileOperaUiState()

    data class Success(val all: Boolean) : FileOperaUiState()
    data class Error(val messageResId: Int) : FileOperaUiState()
}