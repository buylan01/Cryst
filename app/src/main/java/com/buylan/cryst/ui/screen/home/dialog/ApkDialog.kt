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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.buylan.cryst.R
import com.buylan.cryst.ui.component.ApkInfoColumn
import com.buylan.cryst.util.install
import com.buylan.cryst.vfs.LocalFile

@Composable
fun ApkDialog(
    context: Context,
    targetFile: LocalFile,
    onDismiss: () -> Unit,
    unpack: () -> Unit
) {
    val pm = context.packageManager
    val apkInfo = try { pm.getPackageArchiveInfo(targetFile.path, 0) } catch (_: Exception) { null }
    apkInfo?.let {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    apkInfo.applicationInfo!!.apply {
                        sourceDir = targetFile.absolutePath
                        publicSourceDir = targetFile.absolutePath
                    }

                    val installedInfo = try {
                        pm.getApplicationInfo(apkInfo.packageName, 0)
                    } catch (_: PackageManager.NameNotFoundException) {
                        null
                    }

                    Row(
                        modifier = Modifier.padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        val icon = apkInfo.applicationInfo!!.loadIcon(pm)

                        AsyncImage(
                            model = icon,
                            contentDescription = "App icon",
                            modifier = Modifier.size(48.dp),
                            contentScale = ContentScale.Fit,
                            placeholder = ColorPainter(Color.LightGray)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = apkInfo.applicationInfo!!.loadLabel(pm).toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = apkInfo.versionName ?: "Unknown",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    Spacer(Modifier.height(8.dp))

                    ApkInfoColumn(apkInfo, false,installedInfo)
                }
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
                Button(
                    onClick = {
                        install(context, targetFile.toFile())
                    }
                ) {
                    Text(stringResource(R.string.install))
                }
            }
        )
    } ?: Toast.makeText(context, "无法获取安装包信息", Toast.LENGTH_SHORT).show()
}