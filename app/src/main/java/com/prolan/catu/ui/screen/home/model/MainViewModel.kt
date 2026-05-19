package com.prolan.catu.ui.screen.home.model

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prolan.catu.R
import com.prolan.catu.activity.FontActivity
import com.prolan.catu.activity.ImageActivity
import com.prolan.catu.activity.TextEditorActivity
import com.prolan.catu.activity.VideoActivity
import com.prolan.catu.ui.screen.home.PanelStates
import com.prolan.catu.util.RootPath
import com.prolan.catu.util.accessFiles
import com.prolan.catu.util.getFileType
import com.prolan.catu.util.isRootPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

class MainViewModel : ViewModel() {
    var currentPanel by mutableStateOf(PanelPosition.L)
        private set
    fun setPanel(panel: PanelPosition) {
        currentPanel = panel
    }
    var currentPath by mutableStateOf(Path(RootPath))
        private set
    fun setPath(path: Path) {
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
            state.path = state.path.parent
            currentPath = state.path
        }
    }
    fun refreshPanel(panelState: PanelStates) {
        viewModelScope.launch {
            panelState.files = withContext(Dispatchers.IO) {
                if (!panelState.path.isDirectory()) {
                    panelState.highLightFiles = setOf(panelState.path.name)
                    panelState.path = panelState.path.parent
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
    fun handleFileClick(context: Context, file: File, type: FileType? = null) {
        if (file.isDirectory) {
            currentPanelState().path = file.toPath()
        } else {
            when (type ?: getFileType(file)) {
                FileType.TEXT -> {
                    context.startActivity(
                        Intent(
                            context,
                            TextEditorActivity::class.java
                        ).putExtra("filePath", file.path)
                    )
                }

                FileType.IMAGE -> context.startActivity(
                    Intent(
                        context,
                        ImageActivity::class.java
                    ).putExtra("filePath", file.path)
                )

                FileType.INSTALLABLE -> {
                    apkDialog = CommonDialogState(file)
                }

                FileType.FONT -> context.startActivity(
                    Intent(
                        context,
                        FontActivity::class.java
                    ).putExtra("filePath", file.path)
                )

                FileType.VIDEO -> context.startActivity(
                    Intent(
                        context,
                        VideoActivity::class.java
                    ).putExtra("filePath", file.path)
                )

                FileType.AUDIO -> {
                    audioDialog = CommonDialogState(file)
                }

                else -> {
                    openWithDialog = CommonDialogState(file)
                }
            }
        }
    }

    fun onToolAction(action: ToolAction, file: File) {
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
            else -> {

            }
        }
    }
}

enum class ToolAction { Move, Copy, Rename, Delete, Properties, OpenWith, Compress }
enum class SortType(
    val label: Int
) {
    NAME(R.string.name),
    SIZE(R.string.size),
    TIME(R.string.time),
    TYPE(R.string.type)
}
enum class PanelPosition { L, R }
data class CommonDialogState(val file: File)
data class ExtraDialogState(val file: File, val path: Path)
data class PathDialogState(val path: Path)
enum class FileType(
    val label: Int,
    val icon: Int
) {
    FOLDER(R.string.folder, R.drawable.file_folder),
    FILE(R.string.file, R.drawable.file_file),
    TEXT(R.string.text, R.drawable.file_text),
    AUDIO(R.string.audio, R.drawable.file_audio),
    IMAGE(R.string.image, R.drawable.file_image),
    VIDEO(R.string.video, R.drawable.file_video),
    ARCHIVE(R.string.archive, R.drawable.file_zip),
    INSTALLABLE(R.string.installable, R.drawable.file_apk),
    SCRIPT(R.string.script, R.drawable.file_text),
    FONT(R.string.font, R.drawable.file_font)
}

sealed class FileOperaUiState {
    object Idle : FileOperaUiState()
    object InProgress : FileOperaUiState()
    data class Progress(val current: Int, val total: Int, val percentage: Int, val failed: Int) : FileOperaUiState()
    data class Success(val all: Boolean) : FileOperaUiState()
    data class Error(val message: String) : FileOperaUiState()
}