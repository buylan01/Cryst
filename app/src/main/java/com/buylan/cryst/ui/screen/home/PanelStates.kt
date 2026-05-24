package com.buylan.cryst.ui.screen.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.buylan.cryst.ui.screen.home.model.SortType
import com.buylan.cryst.util.RootPath
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile
import java.io.File
import kotlin.io.path.Path

class PanelStates {
    var path by mutableStateOf<VirtualFile>(LocalFile(File(RootPath)))
    var highLightFiles by mutableStateOf(emptySet<String>())

    var files by mutableStateOf(emptyList<VirtualFile>())
    var sortType by mutableStateOf(SortType.NAME)
    lateinit var listState: LazyListState
}