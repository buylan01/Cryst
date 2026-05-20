package com.buylan.cryst.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.buylan.cryst.R

class AppViewModel : ViewModel() {
    var darkMode: DarkMode by mutableStateOf(DarkMode.System)
    fun setDarkTheme(theme: DarkMode) {
        darkMode = theme
    }
    fun isDarkMode(isSystemDarkMode: Boolean = false) : Boolean {
        return when (darkMode) {
            DarkMode.System -> isSystemDarkMode
            DarkMode.Light  -> false
            DarkMode.Dark   -> true
        }
    }
}

enum class DarkMode(
    val label: Int
) {
    System(R.string.system),
    Light(R.string.light),
    Dark(R.string.dark)
}