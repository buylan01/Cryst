package com.buylan.cryst.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buylan.cryst.Application
import com.buylan.cryst.model.AppViewModel
import com.buylan.cryst.ui.screen.home.MainScreen
import com.buylan.cryst.ui.screen.home.model.MainViewModel
import com.buylan.cryst.ui.theme.CatuTheme
import com.buylan.cryst.util.textEditorStartup
import kotlin.io.path.Path

class MainActivity : ComponentActivity() {

    var initPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.isNavigationBarContrastEnforced = false

        setContent {
            val appViewModel = (applicationContext as Application).appViewModel

            val mainViewModel :MainViewModel = viewModel()

            mainViewModel.leftPanelState.listState = rememberLazyListState()
            mainViewModel.rightPanelState.listState = rememberLazyListState()

            if (!initPath.isNullOrEmpty()) {
                mainViewModel.currentPanelState().path = Path(initPath!!)
            }

            val isDark = appViewModel.isDarkMode(isSystemInDarkTheme())

            enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { isDark }
            ))

            CatuTheme(isDark) {
                MainScreen(
                    context = this,
                    viewModel = mainViewModel,
                    appViewModel = appViewModel
                )
            }
        }

        textEditorStartup(applicationContext, true)
    }

    override fun onResume() {
        super.onResume()

        initPath = intent.getStringExtra("path").also { println(it) }
    }
}