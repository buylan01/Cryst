package com.buylan.cryst.ui.screen.texteditor

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditor(
    context: Context,
    filePath: String,
    viewModel: MainViewModel = viewModel(),
    onBack: () -> Unit
) {
    val file = remember { File(filePath) }

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
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {

                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            viewModel.editorState.content = Content(file.readText())
            val state = viewModel.editorState
            val editor = remember {
                setCodeEditorFactory(
                    context = context,
                    state = state,
                    filePath = filePath
                )
            }
            editor.colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())

            val languageScopeName = when(file.extension.lowercase()) {
                "cpp" -> "source.cpp"
                "xml" -> "text.xml"
                "json" -> "source.json"
                "bat" -> "source.batchfile"
                "html" -> "text.html.derivative"
                else -> { null }
            }

            languageScopeName?.let {
                val language = TextMateLanguage.create(
                    languageScopeName, false
                )
                editor.setEditorLanguage(language)
            }

            LaunchedEffect(key1 = state.content) {
                state.editor?.apply {
                    setText(state.content)
                }
            }
            AndroidView(
                factory = { editor },
                modifier = Modifier.fillMaxSize(),
                onRelease = {
                    it.release()
                }
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
    editor.apply {
        setText(state.content)
    }
    state.editor = editor
    return editor
}

class MainViewModel : ViewModel() {
    val editorState by mutableStateOf(
        CodeEditorState()
    )
}