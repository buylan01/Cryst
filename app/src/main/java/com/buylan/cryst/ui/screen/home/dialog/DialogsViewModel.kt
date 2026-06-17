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

package com.buylan.cryst.ui.screen.home.dialog

import androidx.lifecycle.ViewModel
import com.buylan.cryst.ui.screen.home.model.ExtraDialogState
import com.buylan.cryst.ui.screen.home.model.OperationDialogState
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DialogsViewModel : ViewModel() {
    private val _dialogsState = MutableStateFlow(DialogsState())
    val dialogsState: StateFlow<DialogsState> = _dialogsState.asStateFlow()

    // Path Dialog
    fun showPathDialog() {
        _dialogsState.value = _dialogsState.value.copy(pathDialog = true)
    }

    fun hidePathDialog() {
        _dialogsState.value = _dialogsState.value.copy(pathDialog = false)
    }

    // About Dialog
    fun showAboutDialog() {
        _dialogsState.value = _dialogsState.value.copy(aboutDialog = true)
    }

    fun hideAboutDialog() {
        _dialogsState.value = _dialogsState.value.copy(aboutDialog = false)
    }

    // Sort Dialog
    fun showSortDialog() {
        _dialogsState.value = _dialogsState.value.copy(sortDialog = true)
    }

    fun hideSortDialog() {
        _dialogsState.value = _dialogsState.value.copy(sortDialog = false)
    }

    // Permission Request
    fun showPermissionRequest() {
        _dialogsState.value = _dialogsState.value.copy(permissionRequest = true)
    }

    fun hidePermissionRequest() {
        _dialogsState.value = _dialogsState.value.copy(permissionRequest = false)
    }

    // APK Dialog
    fun showApkDialog(data: VirtualFile) {
        _dialogsState.value = _dialogsState.value.copy(apkDialog = data)
    }

    fun hideApkDialog() {
        _dialogsState.value = _dialogsState.value.copy(apkDialog = null)
    }

    // Delete Dialog
    fun showDeleteDialog(data: OperationDialogState) {
        _dialogsState.value = _dialogsState.value.copy(deleteDialog = data)
    }

    fun hideDeleteDialog() {
        _dialogsState.value = _dialogsState.value.copy(deleteDialog = null)
    }

    // Rename Dialog
    fun showRenameDialog(data: VirtualFile) {
        _dialogsState.value = _dialogsState.value.copy(renameDialog = data)
    }

    fun hideRenameDialog() {
        _dialogsState.value = _dialogsState.value.copy(renameDialog = null)
    }

    // Create Dialog
    fun showCreateDialog(data: VirtualFile) {
        _dialogsState.value = _dialogsState.value.copy(createDialog = data)
    }

    fun hideCreateDialog() {
        _dialogsState.value = _dialogsState.value.copy(createDialog = null)
    }

    // Search Dialog
    fun showSearchDialog(data: VirtualFile) {
        _dialogsState.value = _dialogsState.value.copy(searchDialog = data)
    }

    fun hideSearchDialog() {
        _dialogsState.value = _dialogsState.value.copy(searchDialog = null)
    }

    // Audio Dialog
    fun showAudioDialog(data: VirtualFile) {
        _dialogsState.value = _dialogsState.value.copy(audioDialog = data)
    }

    fun hideAudioDialog() {
        _dialogsState.value = _dialogsState.value.copy(audioDialog = null)
    }

    // Properties Dialog
    fun showPropertiesDialog(data: OperationDialogState) {
        _dialogsState.value = _dialogsState.value.copy(propertiesDialog = data)
    }

    fun hidePropertiesDialog() {
        _dialogsState.value = _dialogsState.value.copy(propertiesDialog = null)
    }

    // Open With Dialog
    fun showOpenWithDialog(data: VirtualFile) {
        _dialogsState.value = _dialogsState.value.copy(openWithDialog = data)
    }

    fun hideOpenWithDialog() {
        _dialogsState.value = _dialogsState.value.copy(openWithDialog = null)
    }

    // Compress Dialog
    fun showCompressDialog(data: OperationDialogState) {
        _dialogsState.value = _dialogsState.value.copy(compressDialog = data)
    }

    fun hideCompressDialog() {
        _dialogsState.value = _dialogsState.value.copy(compressDialog = null)
    }

    // Tools Dialog
    fun showToolsDialog(data: OperationDialogState) {
        _dialogsState.value = _dialogsState.value.copy(toolsDialog = data)
    }

    fun hideToolsDialog() {
        _dialogsState.value = _dialogsState.value.copy(toolsDialog = null)
    }

    // Copy Dialog
    fun showCopyDialog(data: ExtraDialogState) {
        _dialogsState.value = _dialogsState.value.copy(copyDialog = data)
    }

    fun hideCopyDialog() {
        _dialogsState.value = _dialogsState.value.copy(copyDialog = null)
    }

    // Move Dialog
    fun showMoveDialog(data: ExtraDialogState) {
        _dialogsState.value = _dialogsState.value.copy(moveDialog = data)
    }

    fun hideMoveDialog() {
        _dialogsState.value = _dialogsState.value.copy(moveDialog = null)
    }
}