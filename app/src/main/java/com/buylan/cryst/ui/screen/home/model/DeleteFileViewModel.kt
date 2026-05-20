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

class DeleteFileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<FileOperaUiState>(FileOperaUiState.Idle)
    val uiState: StateFlow<FileOperaUiState> = _uiState.asStateFlow()

    fun startDelete(target: File) {
        viewModelScope.launch {
            _uiState.value = FileOperaUiState.InProgress
            withContext(Dispatchers.IO) {
                try {
                    if (target.isDirectory) {
                        var failedCount = 0
                        var current = 0
                        val allFiles = target.walkBottomUp().toList()
                        val total = allFiles.size
                        for (file in allFiles) {
                            val deleted = file.delete()
                            if (!deleted) failedCount++
                            current++
                            _uiState.value = FileOperaUiState.Progress(
                                current,
                                total,
                                (current * 100 / total),
                                failedCount
                            )
                        }
                        _uiState.value = FileOperaUiState.Success(failedCount == 0)
                    } else {
                        val deleted = target.delete()
                        _uiState.value =
                            if (deleted) FileOperaUiState.Success(true) else FileOperaUiState.Error("删除文件失败")
                    }
                } catch (e: Exception) {
                    _uiState.value = FileOperaUiState.Error(e.message ?: "未知错误")
                }
            }
        }
    }

    fun finish() {
        _uiState.value = FileOperaUiState.Idle
    }
}