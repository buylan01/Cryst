package com.buylan.cryst.ui.screen.apps.model

import android.graphics.drawable.Drawable

data class ApkInfo (
    val icon: Drawable,
    val label: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val size: Long,
    val isInstalled: Boolean = true,
    val source: String,
    val installedSource: String? = null,
    val dataDir: String? = null,
    val protectedDataDir: String? = null,
    val uid: Int? = null,
    val lastUpdateTime: Long? = null,
    val firstInstallTime: Long? = null,
    val flags: Int? = null
)