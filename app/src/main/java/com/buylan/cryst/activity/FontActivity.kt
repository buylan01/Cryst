package com.buylan.cryst.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buylan.cryst.model.AppViewModel
import com.buylan.cryst.ui.screen.fontviewer.FontViewer
import com.buylan.cryst.ui.theme.CatuTheme

class FontActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.isNavigationBarContrastEnforced = false

        val filePath = intent.getStringExtra("filePath") ?: ""

        if (filePath.isEmpty()) {
            Toast.makeText(this, "字体不存在", Toast.LENGTH_LONG).show()
            this.finish()
        }

        setContent {
            val appViewModel: AppViewModel = viewModel(
                viewModelStoreOwner = applicationContext as ViewModelStoreOwner
            )

            val isDark = appViewModel.isDarkMode(isSystemInDarkTheme())

            enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { isDark }
            ))

            CatuTheme(isDark) {
                FontViewer(this, filePath)
            }
        }
    }
}