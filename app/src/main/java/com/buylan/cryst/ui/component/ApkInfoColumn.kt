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

package com.buylan.cryst.ui.component

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.util.formatFileSize
import java.io.File

@Composable
fun ApkInfoColumn(
    info: PackageInfo,
    alwaysInstalled: Boolean = false,
    installedInfo: ApplicationInfo? = null
) {
    Column {
        InfoItem(R.string.package_name, info.packageName)
        InfoItem(R.string.version_code, info.longVersionCode.toString())
        InfoItem(R.string.size, formatFileSize(File(info.applicationInfo!!.sourceDir).length()))
        if (!alwaysInstalled)
            InfoItem(R.string.installed, (installedInfo != null).toString())
        val appInfo = when {
            alwaysInstalled -> info.applicationInfo
            installedInfo != null -> installedInfo
            else -> null
        }
        appInfo?.let {
            InfoItem(R.string.source_dir, it.sourceDir)
            InfoItem(R.string.data_dir, it.dataDir)
            InfoItem(R.string.uid, it.uid.toString())
        }
    }
}

@Composable
fun InfoItem(title: Int, summary: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(title), modifier = Modifier.width(64.dp))
        Spacer(Modifier.width(16.dp))
        SelectionContainer {
            Text(
                text = summary,
                softWrap = false,
                modifier = Modifier.fillMaxWidth(),
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Left
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}