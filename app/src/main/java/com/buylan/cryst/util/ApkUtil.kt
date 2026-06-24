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

package com.buylan.cryst.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.buylan.cryst.ui.screen.apps.model.ApkInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun install(context: Context, file: File) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", file)

        setDataAndType(uri, "application/vnd.android.package-archive")
    }
    if (!context.packageManager.canRequestPackageInstalls()) {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = "package:${context.packageName}".toUri()
            }
        )
        return
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "未找到安装程序", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "安装失败: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

suspend fun PackageInfo.toApkInfo(packageManager: PackageManager, installed: Boolean = true): ApkInfo {

    val icon = withContext(Dispatchers.Default) {
        applicationInfo!!.loadIcon(packageManager).toBitmap().asImageBitmap()
    }
    val label = applicationInfo!!.loadLabel(packageManager).toString()
    val size = File(applicationInfo!!.sourceDir).length()

    return ApkInfo(
        icon = icon,
        label = label,
        packageName = packageName,
        versionName = versionName.toString(),
        versionCode = longVersionCode,
        size = size,
        isInstalled = installed,
        source = applicationInfo!!.sourceDir,
        dataDir = applicationInfo!!.dataDir,
        protectedDataDir = applicationInfo!!.deviceProtectedDataDir,
        uid = applicationInfo!!.uid
    )
}