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
    var path by mutableStateOf(DefaultPath)
    var highLightFiles by mutableStateOf(emptySet<String>())
    var selectedFiles = mutableStateSetOf<String>()
    var files by mutableStateOf(emptyList<VirtualFile>())
    var sortType by mutableStateOf(SortType.NAME)
    val selectionMode: Boolean
        get() = selectedFiles.isNotEmpty()
    private var rangeAnchorPath: String? = null

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