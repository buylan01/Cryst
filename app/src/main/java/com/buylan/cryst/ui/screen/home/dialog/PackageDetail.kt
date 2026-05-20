package com.buylan.cryst.ui.screen.home.dialog

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.buylan.cryst.R
import com.buylan.cryst.util.formatFileSize
import com.buylan.cryst.util.install
import java.io.File

@Composable
fun PackageDetail(
    onDismiss: () -> Unit,
    context: Context,
    targetFile: File
) {
    val pm = context.packageManager
    AlertDialog(
        onDismissRequest = { onDismiss() },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                val apkInfo = try { pm.getPackageArchiveInfo(targetFile.path, 0) } catch (_: Exception) { null }

                if (apkInfo != null) {

                    apkInfo.applicationInfo!!.apply {
                        sourceDir = targetFile.absolutePath
                        publicSourceDir = targetFile.absolutePath
                    }

                    val installed = try {
                        pm.getApplicationInfo(apkInfo.packageName, 0)
                        true
                    } catch (_: PackageManager.NameNotFoundException) {
                        false
                    }

                    val pkgInfo = if (installed) pm.getPackageInfo(apkInfo.packageName, 0) else null

                    val packageInfo = PackageData(
                        label = apkInfo.applicationInfo!!.loadLabel(pm).toString(),
                        uid = if (installed) pkgInfo!!.applicationInfo!!.uid else null,
                        versionName = apkInfo.versionName ?: "Unknown",
                        versionCode = apkInfo.longVersionCode,
                        packageName = apkInfo.packageName,
                        icon = apkInfo.applicationInfo!!.loadIcon(pm),
                        sourceDir = if (installed) pkgInfo!!.applicationInfo!!.sourceDir else null,
                        dataDir = if (installed)pkgInfo!!.applicationInfo!!.dataDir else null
                    )

                    Row(
                        modifier = Modifier.padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        AsyncImage(
                            model = packageInfo.icon,
                            contentDescription = "App icon",
                            modifier = Modifier.size(48.dp),
                            contentScale = ContentScale.Fit,
                            placeholder = ColorPainter(Color.LightGray)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = packageInfo.label,
                                style = MaterialTheme.typography.bodyLarge,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 160.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = packageInfo.versionName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    Spacer(Modifier.height(8.dp))

                    Column {
                        @Composable
                        fun InfoItem(title: String, summary: String) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(title)
                                Text(
                                    text = summary,
                                    softWrap = false,
                                    modifier = Modifier
                                        .widthIn(max = 160.dp)
                                        .combinedClickable(
                                            onClick = {

                                            }),
                                    overflow = TextOverflow.MiddleEllipsis,
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        InfoItem("包名", packageInfo.packageName)
                        InfoItem("版本号", packageInfo.versionCode.toString())
                        InfoItem("安装状态", if (installed) "已安装" else "未安装")
                        InfoItem("大小", formatFileSize(File(targetFile.path).length()))
                        if (installed) {
                            InfoItem("数据目录", packageInfo.dataDir!!)
                            InfoItem("安装目录", packageInfo.sourceDir!!)
                            InfoItem("UID", packageInfo.uid.toString())
                        }
                    }
                } else {
                    Text("无法获取安装包信息", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    //I should complete it
                }
            ) {
                Text(stringResource(R.string.unpack))
            }
            Button(
                onClick = {
                    install(context, targetFile)
                }
            ) {
                Text(stringResource(R.string.install))
            }
        },
        dismissButton = {
//            TextButton(
//                onClick = {
//
//                }
//            ) {
//                Text(stringResource(R.string.tools))
//            }
        }
    )
}

data class PackageData(
    val label: String,
    val icon: Drawable,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val sourceDir: String? = null,
    val dataDir: String? = null,
    val uid: Int? = null
)