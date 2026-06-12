package com.buylan.cryst.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.buylan.cryst.Application
import com.buylan.cryst.ui.screen.apps.AppsScreen
import com.buylan.cryst.ui.theme.CrystTheme

class AppsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.isNavigationBarContrastEnforced = false

        setContent {
            val appViewModel = (applicationContext as Application).appViewModel

            val isDark = appViewModel.isDarkMode(isSystemInDarkTheme())

            enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { isDark }
            ))

            CrystTheme(isDark) {
                AppsScreen()
            }
        }
    }
}