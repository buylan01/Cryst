package com.buylan.cryst.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buylan.cryst.Application
import com.buylan.cryst.ui.screen.home.MainScreen
import com.buylan.cryst.ui.screen.home.model.MainViewModel
import com.buylan.cryst.ui.theme.CrystTheme
import com.buylan.cryst.util.textEditorStartup
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val pathFlow = MutableSharedFlow<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.isNavigationBarContrastEnforced = false

        setContent {
            val appViewModel = (applicationContext as Application).appViewModel

            val mainViewModel :MainViewModel = viewModel()

            val isDark = appViewModel.isDarkMode(isSystemInDarkTheme())

            enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { isDark }
            ))

            CrystTheme(isDark) {
                MainScreen(
                    context = this,
                    viewModel = mainViewModel,
                    appViewModel = appViewModel,
                    pathFlow = pathFlow
                )
            }

            textEditorStartup(applicationContext, isDark)
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        intent.getStringExtra("path")?.let {
            lifecycleScope.launch {
                pathFlow.emit(it)
            }
        }
    }
}