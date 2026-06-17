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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import com.buylan.cryst.util.DefaultPath
import com.buylan.cryst.util.FileSortType
import com.buylan.cryst.util.isRootPath
import com.buylan.cryst.vfs.VirtualFile

class PanelStates {
    var pathState = mutableStateOf(DefaultPath)
    private val history = mutableListOf(DefaultPath)
    private var historyIndex = 0
    private var isUndoRedo = false
    var path: VirtualFile
        get() = pathState.value
        set(value) {
            if (value != pathState.value) {
                if (!isUndoRedo) {
                    addHistory(value)
                }
                pathState.value = value
            }
        }
    var highLightFiles by mutableStateOf(emptySet<String>())
    var selectedFiles = mutableStateSetOf<String>()
    var files by mutableStateOf(emptyList<VirtualFile>())
    var sortType by mutableStateOf(FileSortType.NAME)
    val selectionMode: Boolean
        get() = selectedFiles.isNotEmpty()
    private var rangeAnchorPath: String? = null

    val canUnNavigate: Boolean
        get() = historyIndex > 0

    val canReNavigate: Boolean
        get() = historyIndex < history.size - 1

    fun navigateBack() {
        if (selectedFiles.isEmpty()) {
            if (!path.isRootPath()) {
                path = path.parentFile ?: DefaultPath
                resetHighLight()
                resetSelection()
            }
        } else {
            resetSelection()
        }
    }

    private fun addHistory(newPath: VirtualFile) {
        while (history.size > historyIndex + 1) {
            history.removeAt(history.size - 1)
        }
        history.add(newPath)
        historyIndex = history.size - 1
    }

    fun unNavigate() {
        if (canUnNavigate) {
            isUndoRedo = true
            historyIndex--
            path = history[historyIndex]
            isUndoRedo = false
        }
    }

    fun reNavigate() {
        if (canReNavigate) {
            isUndoRedo = true
            historyIndex++
            path = history[historyIndex]
            isUndoRedo = false
        }
    }

    fun resetHighLight() {
        highLightFiles = emptySet()
    }

    fun resetSelection() {
        rangeAnchorPath = null
        selectedFiles.clear()
    }

    fun toggleSelection(file: VirtualFile) {
        rangeAnchorPath = null
        if (file.path in selectedFiles) {
            selectedFiles.remove(file.path)
        } else {
            selectedFiles.add(file.path)
        }
    }

    fun swipeSelect(file: VirtualFile) {
        if (rangeAnchorPath == null) {
            rangeAnchorPath = file.path
            if (file.path !in selectedFiles) {
                selectedFiles.add(file.path)
            }
            return
        }
        selectRange(file)
    }

    private fun selectRange(file: VirtualFile) {
        val startIndex = files.indexOfFirst { it.path == rangeAnchorPath }
        val endIndex = files.indexOfFirst { it.path == file.path }

        if (startIndex == -1 || endIndex == -1) return

        val range = if (startIndex <= endIndex) {
            files.subList(startIndex, endIndex + 1)
        } else {
            files.subList(endIndex, startIndex + 1)
        }
        selectedFiles.addAll(range.map { it.path })
        rangeAnchorPath = null
    }
}