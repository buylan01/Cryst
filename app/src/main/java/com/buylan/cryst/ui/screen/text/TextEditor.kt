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

package com.buylan.cryst.ui.screen.text

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buylan.cryst.R
import com.buylan.cryst.util.setEditorTheme
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditor(
    context: Context,
    filePath: String,
    isDark: Boolean,
    viewModel: EditorViewModel = viewModel(),
    onBack: () -> Unit
) {
    val file = remember { File(filePath) }
    val scope = rememberCoroutineScope()

    viewModel.editorState.content = Content(file.readText())
    val state = viewModel.editorState
    val editor = remember {
        setCodeEditorFactory(
            context = context, state = state, filePath = filePath
        )
    }

    var canUndo by remember { mutableStateOf(false) }
    var canRedo by remember { mutableStateOf(false) }
    var isDirty by remember { mutableStateOf(false) }

    DisposableEffect(editor) {
        val subscriber = editor.subscribeEvent(ContentChangeEvent::class.java) { _, _ ->
            isDirty = editor.isDirty
            canUndo = editor.canUndo() && isDirty
            canRedo = editor.canRedo()
        }
        onDispose { subscriber?.unsubscribe() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(file.name, maxLines = 1)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onBack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { editor.undo() },
                        enabled = canUndo
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_undo),
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = { editor.redo() },
                        enabled = canRedo
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_redo),
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        val text = editor.text.toString()
                                        File(filePath).writeText(text)
                                    }

                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = isDirty
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_save),
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = {  }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LaunchedEffect(isDark) {
                setEditorTheme(isDark)
                state.editor?.colorScheme =
                    TextMateColorScheme.create(ThemeRegistry.getInstance())
            }

            AndroidView(
                factory = { editor },
                modifier = Modifier.fillMaxSize(),
                onRelease = { it.release() }
            )
        }
    }
}

data class CodeEditorState(
    var editor: CodeEditor? = null,
    var content: Content = Content()
)

private fun setCodeEditorFactory(
    context: Context,
    state: CodeEditorState,
    filePath: String
): CodeEditor {
    val editor = CodeEditor(context)
    val languageScopeName = when(File(filePath).extension.lowercase()) {
        "cpp" -> "source.cpp"
        "xml" -> "text.xml"
        "json" -> "source.json"
        "bat" -> "source.batchfile"
        "html" -> "text.html.basic"
        else -> { null }
    }

    languageScopeName?.let {
        val language = TextMateLanguage.create(
            languageScopeName, false
        )
        editor.setEditorLanguage(language)
    }
    editor.apply {
        setText(state.content)
        isStickyTextSelection = true
        isUndoEnabled = true
    }
    state.editor = editor
    return editor
}