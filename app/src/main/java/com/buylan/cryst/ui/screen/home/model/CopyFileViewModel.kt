package com.buylan.cryst.ui.screen.home.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buylan.cryst.R
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CopyFileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<FileOperaUiState>(FileOperaUiState.Idle)
    val uiState: StateFlow<FileOperaUiState> = _uiState.asStateFlow()

    fun startCopy(source: List<VirtualFile>, targetDir: VirtualFile) {
        viewModelScope.launch {
            _uiState.value = FileOperaUiState.InProgress
            withContext(Dispatchers.IO) {
                try {
                    var failedCount = 0
                    var current = 0
                    val allFiles = source.flatMap { root ->
                        if (root.isDirectory) {
                            root.walkTopDown().map { root to it }
                        } else {
                            listOf(root to root)
                        }
                    }
                    val total = allFiles.size
                    for ((root, file) in allFiles) {
                        try {
                            val relativePath = file.relativeTo(root)
                            val targetFile = if (root.isDirectory) {
                                LocalFile(parent = targetDir.absolutePath, child="${root.name}/$relativePath")
                            } else {
                                LocalFile(targetDir.absolutePath, root.name)
                            }
                            if (file.isDirectory) {
                                targetFile.mkdirs()
                            } else {
                                targetFile.parentFile?.mkdirs()
                                file.copyTo(
                                    target = targetFile, overwrite = true
                                )
                            }
                        } catch (e: Exception) {
                            println(e)
                            failedCount++
                        }
                        current++
                        _uiState.value = FileOperaUiState.Progress(
                            current = current,
                            total = total,
                            percentage = (current * 100 / total),
                            failed = failedCount
                        )
                    }
                    _uiState.value = FileOperaUiState.Success(failedCount == 0)
                } catch (e: Exception) {
                    _uiState.value = FileOperaUiState.Error(R.string.copy_to_failed)
                }
            }
        }
    }

    fun finish() {
        _uiState.value = FileOperaUiState.Idle
    }
}