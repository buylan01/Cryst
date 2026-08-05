package com.buylan.cryst.ui.component

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.SubcomposeAsyncImage
import com.android.apksig.ApkVerifier
import com.buylan.cryst.R
import com.buylan.cryst.ui.screen.apps.model.ApkInfo
import com.buylan.cryst.util.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class MenuType {
    Installed,
    ApkFile
}

@Composable
fun ApkDialogContent(
    info: ApkInfo,
    menuType: MenuType,
    imageRequest: Any,
    imageLoader: ImageLoader
) {
    val context = LocalContext.current
    val pm = context.packageManager
    var signResult by remember { mutableStateOf("Loading") }
    Column(
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SubcomposeAsyncImage(
                model = imageRequest,
                imageLoader = imageLoader,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit,
                loading = { AppIconPlaceholder() },
                error = { AppIconPlaceholder() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = info.label,
                    style = MaterialTheme.typography.bodyLarge,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = info.versionName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            when(menuType) {
                MenuType.Installed -> {
                    Box {
                        var showToolMenu by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { showToolMenu = true },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more_vert),
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = showToolMenu,
                            onDismissRequest = { showToolMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.launch)) },
                                onClick = {
                                    showToolMenu = false
                                    try {
                                        context.startActivity(
                                            pm.getLaunchIntentForPackage(
                                                info.packageName
                                            )
                                        )
                                    } catch (_: NullPointerException) {
                                        Toast.makeText(
                                            context,
                                            "启动失败",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.detail)) },
                                onClick = {
                                    showToolMenu = false
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts(
                                                "package",
                                                info.packageName,
                                                null
                                            )
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete)) },
                                onClick = {
                                    showToolMenu = false
                                    context.startActivity(
                                        Intent(Intent.ACTION_DELETE).apply {
                                            data = Uri.fromParts(
                                                "package",
                                                info.packageName,
                                                null
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
                MenuType.ApkFile -> {

                }
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        @Composable
        fun InfoItem(title: Int, summary: String) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(title),
                    modifier = Modifier.weight(0.3f)
                )
                SelectionContainer(
                    modifier = Modifier.weight(0.7f)
                ) {
                    Text(
                        text = summary,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Left
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                val result = ApkVerifier.Builder(File(info.source))
                    .setMinCheckedPlatformVersion(18)
                    .build()
                    .verify()
                val isVerified = result.isVerified
                val hasV1 =
                    result.v1SchemeSigners.isNotEmpty() || result.v1SchemeIgnoredSigners.isNotEmpty()
                val hasV2 = result.isVerifiedUsingV2Scheme
                val hasV3 = result.isVerifiedUsingV3Scheme
                val hasV31 = result.isVerifiedUsingV31Scheme
                val hasV4 = result.isVerifiedUsingV4Scheme

                val schemes = mutableListOf<String>()
                if (hasV1) schemes.add("v1")
                if (hasV2) schemes.add("v2")
                if (hasV3) schemes.add("v3")
                if (hasV31) schemes.add("v3.1")
                if (hasV4) schemes.add("v4")

                signResult = if (isVerified) "通过 " else "失效 "
                signResult += "(${schemes.joinToString("+")})"
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoItem(title = R.string.package_name, summary = info.packageName)
            InfoItem(title = R.string.version_code, summary = info.versionCode.toString())
            InfoItem(title = R.string.size, summary = formatFileSize(info.size))
            InfoItem(title = R.string.sign, summary = signResult)
            if (info.isInstalled) {
                InfoItem(R.string.source_dir, info.installedSource.toString())
                InfoItem(R.string.data_dir, info.dataDir.toString())
                InfoItem(R.string.uid, info.uid.toString())
            }
        }
    }
}