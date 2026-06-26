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

package com.buylan.cryst.ui.screen.home.dialog

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.buylan.cryst.R
import com.buylan.cryst.ui.component.ApkDialogContent
import com.buylan.cryst.ui.component.MenuType
import com.buylan.cryst.ui.screen.apps.model.ApkInfo
import com.buylan.cryst.util.install
import com.buylan.cryst.util.toApkInfo
import com.buylan.cryst.vfs.LocalFile

@Composable
fun ApkDialog(
    context: Context,
    targetFile: LocalFile,
    onDismiss: () -> Unit,
    unpack: () -> Unit
) {

    val pm = context.packageManager
    var apkInfo by remember { mutableStateOf<ApkInfo?>(null) }

    LaunchedEffect(Unit) {
        val archive = pm.getPackageArchiveInfo(targetFile.path, 0)
        if (archive != null) {
            apkInfo = archive.toApkInfo(pm, installed = false)
            try {
                val installedPkg = pm.getPackageInfo(apkInfo!!.packageName, 0)
                installedPkg?.applicationInfo?.let { installedApp ->
                    apkInfo = apkInfo!!.copy(
                        isInstalled = true,
                        installedSource = installedApp.sourceDir,
                        dataDir = installedApp.dataDir,
                        uid = installedApp.uid,
                    )
                }
            } catch (_: PackageManager.NameNotFoundException) {

            }
        } else {
            onDismiss()
            Toast.makeText(context, "无法获取安装包信息", Toast.LENGTH_SHORT).show()
        }
    }

    apkInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { onDismiss() },
            text = {
                ApkDialogContent(info = info, menuType = MenuType.ApkFile)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        unpack()
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.unpack))
                }
                TextButton(
                    onClick = {
                        install(context, targetFile.toFile())
                    }
                ) {
                    Text(stringResource(R.string.install))
                }
            }
        )
    }
}