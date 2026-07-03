package com.buylan.cryst.ui.screen.home.model

import androidx.compose.ui.graphics.painter.Painter
import java.io.File

data class StorageItem(
    val path: File,
    val icon: Painter,
    val name: String? = null
)