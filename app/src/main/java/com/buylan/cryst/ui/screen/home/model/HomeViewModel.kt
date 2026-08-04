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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buylan.cryst.util.PanelPosition
import com.buylan.cryst.util.ToolAction
import com.buylan.cryst.util.shareFile
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val _dialogsViewModel: DialogsViewModel = DialogsViewModel(),
    private val _leftPanelViewModel: PanelViewModel = PanelViewModel(),
    private val _rightPanelViewModel: PanelViewModel = PanelViewModel()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val dialogsViewModel: DialogsViewModel get() = _dialogsViewModel

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
            panel.refresh()
        }
    }

    fun onToolAction(context: Context, action: ToolAction, file: List<VirtualFile>) {
        _dialogsViewModel.dismiss()

        when (action) {
            ToolAction.Move -> {
                _dialogsViewModel.show(DialogsEvent.MoveDialog(ExtraDialogState(file, anotherPath)))
            }
            ToolAction.Copy -> {
                _dialogsViewModel.show(DialogsEvent.CopyDialog(ExtraDialogState(file, anotherPath)))
            }
            ToolAction.Rename -> {
                val f = file.singleOrNull() ?: return
                _dialogsViewModel.show(DialogsEvent.RenameDialog(f))
            }
            ToolAction.Delete -> {
                _dialogsViewModel.show(DialogsEvent.DeleteDialog(file))
            }
            ToolAction.Properties -> {
                _dialogsViewModel.show(DialogsEvent.PropertiesDialog(file))
            }
            ToolAction.OpenWith -> {
                val f = file.singleOrNull() ?: return
                _dialogsViewModel.show(DialogsEvent.OpenWithDialog(f))
            }
            ToolAction.Share -> {
                context.shareFile(file.singleOrNull() ?: return)
            }
            ToolAction.Compress -> {
                _dialogsViewModel.show(DialogsEvent.CompressDialog(file))
            }
        }
    }
}