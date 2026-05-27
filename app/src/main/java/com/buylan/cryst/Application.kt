package com.buylan.cryst

import android.app.Application
import com.buylan.cryst.model.AppViewModel
import java.io.File

class Application : Application() {
    lateinit var appViewModel: AppViewModel
        private set

    override fun onCreate() {
        super.onCreate()
        appViewModel = AppViewModel(this)
        clearArchiveCache()
    }

    private fun clearArchiveCache() {
        try {
            val dir = File(cacheDir, "archive_cache")
            if (dir.exists()) dir.deleteRecursively()
        } catch (_: Exception) {}
    }

}