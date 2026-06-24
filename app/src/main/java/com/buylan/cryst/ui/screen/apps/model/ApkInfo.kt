package com.buylan.cryst.ui.screen.apps.model

import androidx.compose.ui.graphics.ImageBitmap

data class ApkInfo (
    val icon: ImageBitmap,
    val label: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val size: Long,
    val isInstalled: Boolean = true,
    val source: String? = null,
    val dataDir: String? = null,
    val protectedDataDir: String? = null,
    val uid: Int? = null,
    val lastUpdateTime: Long? = null,
    val firstInstallTime: Long? = null,
    val flags: Int? = null
)