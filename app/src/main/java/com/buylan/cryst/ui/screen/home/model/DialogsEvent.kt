package com.buylan.cryst.ui.screen.home.model

import com.buylan.cryst.vfs.VirtualFile

sealed interface DialogsEvent {
    data object PermissionRequest : DialogsEvent
    data object PathDialog : DialogsEvent
    data object AboutDialog : DialogsEvent
    data object SortDialog : DialogsEvent

    data class ApkDialog(val file: VirtualFile) : DialogsEvent
    data class RenameDialog(val file: VirtualFile) : DialogsEvent
    data class CreateDialog(val parent: VirtualFile) : DialogsEvent
    data class SearchDialog(val root: VirtualFile) : DialogsEvent
    data class AudioDialog(val file: VirtualFile) : DialogsEvent
    data class OpenWithDialog(val file: VirtualFile) : DialogsEvent
    data class RunScriptDialog(val file: VirtualFile) : DialogsEvent

    data class PropertiesDialog(val files: List<VirtualFile>) : DialogsEvent
    data class DeleteDialog(val files: List<VirtualFile>) : DialogsEvent
    data class CompressDialog(val files: List<VirtualFile>) : DialogsEvent
    data class ToolsDialog(val files: List<VirtualFile>) : DialogsEvent

    data class CopyDialog(val state: ExtraDialogState) : DialogsEvent
    data class MoveDialog(val state: ExtraDialogState) : DialogsEvent
}