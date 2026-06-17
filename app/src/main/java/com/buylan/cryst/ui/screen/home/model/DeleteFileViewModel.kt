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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buylan.cryst.R
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

    fun startDelete(targets: List<File>) {
        viewModelScope.launch {
            _uiState.value = FileOperaUiState.InProgress
            withContext(Dispatchers.IO) {
                try {
                    val allFiles = targets.flatMap {
                        if (it.isDirectory) {
                            it.walkBottomUp().toList()
                        } else {
                            listOf(it)
                        }
                    }
                    var failedCount = 0
                    var current = 0
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
                } catch (e: Exception) {
                    _uiState.value = FileOperaUiState.Error(R.string.delete_failed)
                }
            }
        }
    }

    fun finish() {
        _uiState.value = FileOperaUiState.Idle
    }
}