package com.buylan.cryst.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import com.buylan.cryst.ui.screen.home.model.SortType
import com.buylan.cryst.util.DefaultPath
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
    var sortType by mutableStateOf(SortType.NAME)
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
            history.removeAt(history.size)
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