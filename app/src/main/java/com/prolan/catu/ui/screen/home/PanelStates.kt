package com.prolan.catu.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.prolan.catu.ui.screen.home.model.SortType
import com.prolan.catu.util.RootPath
import java.io.File
import kotlin.io.path.Path

class PanelStates {
    var path by mutableStateOf(Path(RootPath))
    var highLightFiles by mutableStateOf(emptySet<String>())

    var files by mutableStateOf(emptyList<File>())
    var sortType by mutableStateOf(SortType.NAME)
}