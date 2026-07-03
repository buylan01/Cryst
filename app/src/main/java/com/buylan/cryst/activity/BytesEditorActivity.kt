package com.buylan.cryst.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.buylan.cryst.Application
import com.buylan.cryst.ui.screen.bytes.BytesEditor
import com.buylan.cryst.ui.theme.CrystTheme
import java.io.File

class BytesEditorActivity : ComponentActivity() {

    companion object {
        const val EXTRA_FILE_PATH = "filePath"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.isNavigationBarContrastEnforced = false

        setContent {
            val appViewModel = (applicationContext as Application).appViewModel

            val isDark = appViewModel.isDarkMode()

            enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { isDark }
            ))

            val filePath = intent.getStringExtra(EXTRA_FILE_PATH)

            if (filePath == null) {
                Toast.makeText(this, "File not found", Toast.LENGTH_LONG).show()
                this.finish()
            }

            CrystTheme(isDark) {
                BytesEditor(
                    onBack = { this.finish() },
                    file = File(filePath!!)
                )
            }
        }
    }
}