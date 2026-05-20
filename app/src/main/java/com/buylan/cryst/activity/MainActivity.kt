package com.buylan.cryst.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buylan.cryst.model.AppViewModel
import com.buylan.cryst.ui.screen.home.MainScreen
import com.buylan.cryst.ui.theme.CatuTheme
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.isNavigationBarContrastEnforced = false

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
                MainScreen(
                    context = this,
                    appViewModel = appViewModel
                )
            }
        }

        FileProviderRegistry.getInstance().addFileProvider(
            AssetsFileResolver(applicationContext.assets)
        )
        GrammarRegistry.getInstance().loadGrammars("languages.json")
    }
}