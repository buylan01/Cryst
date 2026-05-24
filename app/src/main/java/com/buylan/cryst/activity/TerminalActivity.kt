package com.buylan.cryst.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import com.buylan.cryst.Application
import com.buylan.cryst.ui.screen.terminal.Terminal
import com.buylan.cryst.ui.theme.CatuTheme

class TerminalActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.isNavigationBarContrastEnforced = false
        setContent {
            val appViewModel = (applicationContext as Application).appViewModel

            val isDark = appViewModel.isDarkMode(isSystemInDarkTheme())

            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { true }
            ))

            CatuTheme(true) {
                Terminal(this)
            }
        }
    }
}