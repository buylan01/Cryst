package com.buylan.cryst.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.buylan.cryst.Application
import com.buylan.cryst.ui.screen.videoplayer.VideoViewer
import com.buylan.cryst.ui.theme.CrystTheme

class VideoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.isNavigationBarContrastEnforced = false

        val filePath = intent.getStringExtra("filePath") ?: ""

        if (filePath.isEmpty()) {
            Toast.makeText(this, "视频不存在", Toast.LENGTH_LONG).show()
            this.finish()
        }

        setContent {
            val appViewModel = (applicationContext as Application).appViewModel
            val isDark = appViewModel.isDarkMode(isSystemInDarkTheme())

            enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { isDark }
            ))

            CrystTheme(isDark) {
                VideoViewer(this, filePath)
            }
        }
    }
}