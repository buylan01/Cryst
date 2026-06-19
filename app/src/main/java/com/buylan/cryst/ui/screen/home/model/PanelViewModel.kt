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
import com.buylan.cryst.util.DefaultPath
import com.buylan.cryst.util.FileSortType
import com.buylan.cryst.util.isRootPath
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PanelViewModel : ViewModel() {

    private val _panelStates = MutableStateFlow(PanelStates())
    val panelStates: StateFlow<PanelStates> = _panelStates.asStateFlow()

    private val _scrollToIndex = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val scrollToIndex = _scrollToIndex.asSharedFlow()

    val canUnNavigate: Boolean
        get() = _panelStates.value.historyIndex > 0

    val canReNavigate: Boolean
        get() = _panelStates.value.historyIndex < _panelStates.value.history.size - 1

    val selectionMode: Boolean
        get() = _panelStates.value.selectedFiles.isNotEmpty()

    fun setPath(path: VirtualFile) {
        if (path != _panelStates.value.path) {
            _panelStates.update {
                val newHistory = it.history.toMutableList()
                while (newHistory.size > it.historyIndex + 1) {
                    newHistory.removeAt(newHistory.size - 1)
                }
                newHistory.add(path)
                it.copy(
                    history = newHistory,
                    historyIndex = newHistory.size - 1,
                    path = path
                )
            }
        }
    }

    fun navigateBack() {
        _panelStates.update {
            if (it.selectedFiles.isEmpty()) {
                if (!it.path.isRootPath()) {
                    it.copy(
                        path = it.path.parentFile ?: DefaultPath,
                        highLightFiles = emptySet(),
                        selectedFiles = emptySet(),
                        rangeAnchorPath = null
                    )
                } else {
                    it
                }
            } else {
                it.copy(
                    selectedFiles = emptySet(),
                    rangeAnchorPath = null
                )
            }
        }
    }

    fun unNavigate() {
        if (canUnNavigate) {
            _panelStates.update {
                it.copy(
                    historyIndex = it.historyIndex - 1,
                    path = it.history[it.historyIndex],
                )
            }
        }
    }

    fun reNavigate() {
        if (canReNavigate) {
            _panelStates.update {
                it.copy(
                    historyIndex = it.historyIndex + 1,
                    path = it.history[it.historyIndex]
                )
            }
        }
    }

    suspend fun refresh(fileLoader: suspend (path: VirtualFile, sortType: FileSortType) -> List<VirtualFile>) {
        val currentState = _panelStates.value
        if (!currentState.path.isDirectory) {
            val highlightName = currentState.path.name
            val parent = currentState.path.parentFile ?: return
            _panelStates.update {
                it.copy(
                    path = parent,
                    highLightFiles = setOf(highlightName)
                )
            }
        }

        val state = _panelStates.value
        val files = fileLoader(state.path, state.sortType)

        _panelStates.update {
            it.copy(
                files = files,
                selectedFiles = emptySet(),
                rangeAnchorPath = null
            )
        }

        val highlightSet = _panelStates.value.highLightFiles
        if (highlightSet.isNotEmpty()) {
            val index = files.indexOfFirst { it.name.equals(highlightSet.first(), ignoreCase = true) }
            if (index != -1) {
                _scrollToIndex.emit(index)
            }
        }
    }

    fun toggleSelection(file: VirtualFile) {
        _panelStates.update {
            val newSelection = it.selectedFiles.toMutableSet()
            if (file.path in newSelection) {
                newSelection.remove(file.path)
            } else {
                newSelection.add(file.path)
            }
            it.copy(
                selectedFiles = newSelection,
                rangeAnchorPath = null
            )
        }
    }

    fun swipeSelect(file: VirtualFile) {
        _panelStates.update { current ->
            val anchor = current.rangeAnchorPath
            if (anchor == null) {
                val newSelection = current.selectedFiles.toMutableSet()
                if (file.path !in newSelection) {
                    newSelection.add(file.path)
                }
                current.copy(
                    rangeAnchorPath = file.path,
                    selectedFiles = newSelection
                )
            } else {
                val startIndex = current.files.indexOfFirst { it.path == anchor }
                val endIndex = current.files.indexOfFirst { it.path == file.path }

                if (startIndex == -1 || endIndex == -1) {
                    current
                } else {
                    val range = if (startIndex <= endIndex) {
                        current.files.subList(startIndex, endIndex + 1)
                    } else {
                        current.files.subList(endIndex, startIndex + 1)
                    }
                    val newSelection = current.selectedFiles.toMutableSet().apply {
                        addAll(range.map { it.path })
                    }
                    current.copy(
                        selectedFiles = newSelection,
                        rangeAnchorPath = null
                    )
                }
            }
        }
    }

    fun setSort(sortType: FileSortType) {
        _panelStates.update {
            it.copy(
                sortType = sortType
            )
        }
    }
}