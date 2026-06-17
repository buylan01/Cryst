/*
 *    Copyright 2026 buylan
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
import com.buylan.cryst.Application
import com.buylan.cryst.ui.screen.home.HomeScreen
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
            val isDark = appViewModel.isDarkMode(isSystemInDarkTheme())

            enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { isDark }
            ))

            CrystTheme(isDark) {
                HomeScreen(
                    context = this,
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