package com.buylan.cryst.ui.screen.home.model

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buylan.cryst.R
import com.buylan.cryst.activity.FontActivity
import com.buylan.cryst.activity.ImageActivity
import com.buylan.cryst.activity.TextEditorActivity
import com.buylan.cryst.activity.VideoActivity
import com.buylan.cryst.ui.screen.home.PanelStates
import com.buylan.cryst.util.RootPath
import com.buylan.cryst.util.accessFiles
import com.buylan.cryst.util.getActualFile
import com.buylan.cryst.util.getFileType
import com.buylan.cryst.util.isRootPath
import com.buylan.cryst.util.shareFile
import com.buylan.cryst.vfs.ArchiveFile
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    var currentPanel by mutableStateOf(PanelPosition.L)
        private set

    fun setPanel(panel: PanelPosition) {
        currentPanel = panel
    }

    var currentPath by mutableStateOf<VirtualFile>(LocalFile(RootPath))
        private set

    fun setPath(path: VirtualFile) {
        currentPath = path
    }

    var showPathDialog by mutableStateOf(false)
    var showAboutDialog by mutableStateOf(false)
    var showSort by mutableStateOf(false)
    var showPermissionRequest by mutableStateOf(!Environment.isExternalStorageManager())
    var apkDialog: CommonDialogState? by mutableStateOf(null)
    var deleteDialog: CommonDialogState? by mutableStateOf(null)
    var renameDialog: CommonDialogState? by mutableStateOf(null)
    var createDialog: PathDialogState? by mutableStateOf(null)
    var searchDialog: PathDialogState? by mutableStateOf(null)
    var audioDialog: CommonDialogState? by mutableStateOf(null)
    var propertiesDialog: CommonDialogState? by mutableStateOf(null)
    var openWithDialog: CommonDialogState? by mutableStateOf(null)
    var toolsDialog: CommonDialogState? by mutableStateOf(null)
    var copyDialog: ExtraDialogState? by mutableStateOf(null)
    var moveDialog: ExtraDialogState? by mutableStateOf(null)
    private val _scrollToIndex = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val scrollToIndex = _scrollToIndex.asSharedFlow()
    val leftPanelState = PanelStates()
    val rightPanelState = PanelStates()

    fun currentPanelState() = if (currentPanel == PanelPosition.L) leftPanelState else rightPanelState
    fun uncurrentPanelState() = if (currentPanel == PanelPosition.R) leftPanelState else rightPanelState
    fun navigateBack() {
        val state = currentPanelState()
        if (!currentPath.isRootPath()) {
            state.path = state.path.parent!!
            currentPath = state.path
        }
    }
    fun refreshPanel(panelState: PanelStates) {
        viewModelScope.launch {
            panelState.files = withContext(Dispatchers.IO) {
                if (!panelState.path.isDirectory) {
                    panelState.highLightFiles = setOf(panelState.path.name)
                    panelState.path = panelState.path.parent!!
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
        }
    }

    fun handleFileClick(context: Context, file: VirtualFile, type: FileType? = null) {
        if (file.isDirectory) {
            currentPanelState().path = file
        } else {
            val actualType = type ?: getFileType(file)
            val actualFile = getActualFile(context, file)

            when (actualType) {
                FileType.TEXT -> {
                    context.startActivity(
                        Intent(
                            context,
                            TextEditorActivity::class.java
                        ).putExtra("filePath", actualFile.path)
                    )
                }

                FileType.ARCHIVE -> {
                    try {
                        currentPanelState().path = ArchiveFile(entranceFile = actualFile)
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                }

                FileType.IMAGE -> context.startActivity(
                    Intent(
                        context,
                        ImageActivity::class.java
                    ).putExtra("filePath", actualFile.path)
                )

                FileType.INSTALLABLE -> {
                    apkDialog = CommonDialogState(actualFile)
                }

                FileType.FONT -> context.startActivity(
                    Intent(
                        context,
                        FontActivity::class.java
                    ).putExtra("filePath", actualFile.path)
                )

                FileType.VIDEO -> context.startActivity(
                    Intent(
                        context,
                        VideoActivity::class.java
                    ).putExtra("filePath", actualFile.path)
                )

                FileType.AUDIO -> {
                    audioDialog = CommonDialogState(actualFile)
                }

                else -> {
                    openWithDialog = CommonDialogState(actualFile)
                }
            }
        }
    }

    fun onToolAction(context: Context, action: ToolAction, file: VirtualFile) {
        toolsDialog = null
        when (action) {
            ToolAction.Move -> {
                moveDialog = ExtraDialogState(file, uncurrentPanelState().path)
            }

            ToolAction.Copy -> {
                copyDialog = ExtraDialogState(file, uncurrentPanelState().path)
            }

            ToolAction.Rename -> {
                renameDialog = CommonDialogState(file)
            }

            ToolAction.Delete -> {
                deleteDialog = CommonDialogState(file)
            }

            ToolAction.Properties -> {
                propertiesDialog = CommonDialogState(file)
            }

            ToolAction.OpenWith -> {
                openWithDialog = CommonDialogState(file)
            }

            ToolAction.Share -> {
                context.shareFile(file)
            }

            ToolAction.Compress -> {

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
data class CommonDialogState(val file: VirtualFile)
data class ExtraDialogState(val file: VirtualFile, val path: VirtualFile)
data class PathDialogState(val path: VirtualFile)
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
    INSTALLABLE(R.string.installable, R.drawable.ic_apk_document),
    SCRIPT(R.string.script, R.drawable.ic_terminal_2),
    FONT(R.string.font, R.drawable.ic_font_download)
}

sealed class FileOperaUiState {
    object Idle : FileOperaUiState()
    object InProgress : FileOperaUiState()
    data class Progress(val current: Int, val total: Int, val percentage: Int, val failed: Int) :
        FileOperaUiState()

    data class Success(val all: Boolean) : FileOperaUiState()
    data class Error(val message: String) : FileOperaUiState()
}