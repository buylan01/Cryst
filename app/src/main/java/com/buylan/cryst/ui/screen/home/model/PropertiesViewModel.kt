package com.buylan.cryst.ui.screen.home.model

import androidx.lifecycle.ViewModel
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class PropertiesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PropertiesUiState())
    val uiState: StateFlow<PropertiesUiState> = _uiState.asStateFlow()

    suspend fun compute(files: List<VirtualFile>) = withContext(Dispatchers.Default) {
        val sequence = files.asSequence().flatMap { root ->
            if (root.isDirectory) root.walkTopDownSequence() else sequenceOf(root)
        }

        var totalSize = 0L
        var processed = 0

        for (file in sequence) {
            if (!file.isDirectory) totalSize += file.length()
            processed++
            _uiState.value = PropertiesUiState(
                totalSize = totalSize,
                totalCount = processed,
                isCalculating = true
            )
        }

        _uiState.value = PropertiesUiState(
            totalSize = totalSize,
            totalCount = processed,
            isCalculating = false
        )
    }
}