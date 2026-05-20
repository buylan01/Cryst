package com.buylan.cryst.ui.screen.home.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CopyFileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<FileOperaUiState>(FileOperaUiState.Idle)
    val uiState: StateFlow<FileOperaUiState> = _uiState.asStateFlow()

    fun startCopy(source: File, targetDir: File) {
        viewModelScope.launch {
            _uiState.value = FileOperaUiState.InProgress
            withContext(Dispatchers.IO) {
                try {
                    if (!source.exists()) {
                        _uiState.value = FileOperaUiState.Error("源文件不存在")
                        return@withContext
                    }
                    var failedCount = 0
                    var current = 0
                    val allFiles = if (source.isDirectory) {
                        source.walkTopDown().toList()
                    } else {
                        listOf(source)
                    }
                    val total = allFiles.size
                    for (file in allFiles) {
                        try {
                            val relativePath = file.relativeTo(source).path
                            val targetFile = if (source.isDirectory) {
                                File(targetDir, "${source.name}/$relativePath")
                            } else {
                                File(targetDir, source.name)
                            }
                            if (file.isDirectory) {
                                targetFile.mkdirs()
                            } else {
                                targetFile.parentFile?.mkdirs()
                                file.copyTo(
                                    target = targetFile, overwrite = true
                                )
                            }
                        } catch (_: Exception) {
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
                    _uiState.value = FileOperaUiState.Error(
                        e.message ?: "未知错误"
                    )
                }
            }
        }
    }

    fun finish() {
        _uiState.value = FileOperaUiState.Idle
    }
}