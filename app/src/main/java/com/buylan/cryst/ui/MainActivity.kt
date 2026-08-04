package com.buylan.cryst.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.buylan.cryst.Application
import com.buylan.cryst.ui.screen.apps.AppsScreen
import com.buylan.cryst.ui.screen.bytes.BytesEditor
import com.buylan.cryst.ui.screen.font.FontViewer
import com.buylan.cryst.ui.screen.home.HomeScreen
import com.buylan.cryst.ui.screen.image.ImageViewer
import com.buylan.cryst.ui.screen.libraries.LibrariesScreen
import com.buylan.cryst.ui.screen.settings.SettingsScreen
import com.buylan.cryst.ui.screen.terminal.Terminal
import com.buylan.cryst.ui.screen.text.TextEditor
import com.buylan.cryst.ui.screen.video.VideoViewer
import com.buylan.cryst.ui.theme.CrystTheme
import com.buylan.cryst.util.textEditorStartup
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    private val pathFlow = MutableSharedFlow<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.isNavigationBarContrastEnforced = false

        setContent {
            val appViewModel = (applicationContext as Application).appViewModel
            val isDark = appViewModel.isDarkMode()
            val backStack = rememberNavBackStack(Screen.Home)

            enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { isDark }
            ))

            CrystTheme(isDark) {

                val entryProvider = entryProvider<NavKey> {

                    entry<Screen.Home> {
                        HomeScreen(
                            context = this@MainActivity,
                            onNavigate = {
                                backStack.add(it)
                            },
                            pathFlow = pathFlow,
                            isBackHandlerEnabled = backStack.size == 1
                        )
                    }

                    entry<Screen.Settings> {
                        SettingsScreen(
                            appViewModel = appViewModel,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    entry<Screen.Apps> {
                        AppsScreen(onBack = { backStack.removeLastOrNull() })
                    }

                    entry<Screen.Licenses> {
                        LibrariesScreen(onBack = { backStack.removeLastOrNull() })
                    }

                    entry<Screen.Terminal> {
                        CrystTheme(true) {
                            Terminal(null, onBack = { backStack.removeLastOrNull() })
                        }
                    }

                    entry<Screen.TextEditor> { route ->
                        TextEditor(
                            filePath = route.filePath,
                            isDark = isDark,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    entry<Screen.ImageViewer> { route ->
                        ImageViewer(
                            filePath = route.filePath,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    entry<Screen.VideoPlayer> { route ->
                        VideoViewer(
                            filePath = route.filePath,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    entry<Screen.FontViewer> { route ->
                        FontViewer(
                            filePath = route.filePath,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }

                    entry<Screen.BytesEditor> { route ->
                        BytesEditor(
                            filePath = route.filePath,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                }

                NavDisplay(
                    backStack = backStack,
                    entryProvider = entryProvider,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = spring(
                                dampingRatio = 1f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = spring(
                                dampingRatio = 1f,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    },
                    popTransitionSpec = {
                        slideInHorizontally { -it / 3 } togetherWith slideOutHorizontally { it }
                    },
                    predictivePopTransitionSpec = {
                        slideInHorizontally { -it / 3 } togetherWith slideOutHorizontally { it }
                    }
                )

            }

            textEditorStartup(applicationContext, isDark)
        }

    }
}

@Serializable
sealed class Screen : NavKey {
    @Serializable
    data object Home : Screen()
    data object Settings : Screen()
    data object Apps : Screen()
    data object Terminal : Screen()
    data object Licenses : Screen()
    data class TextEditor(val filePath: String) : Screen()
    data class ImageViewer(val filePath: String) : Screen()
    data class FontViewer(val filePath: String) : Screen()
    data class VideoPlayer(val filePath: String) : Screen()
    data class BytesEditor(val filePath: String) : Screen()
}