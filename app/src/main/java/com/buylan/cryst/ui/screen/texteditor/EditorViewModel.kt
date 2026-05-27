package com.buylan.cryst.ui.screen.texteditor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class EditorViewModel : ViewModel() {
    val editorState by mutableStateOf(
        CodeEditorState()
    )
}