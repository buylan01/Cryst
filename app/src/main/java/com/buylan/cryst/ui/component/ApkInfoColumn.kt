package com.buylan.cryst.ui.component

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
        if (!alwaysInstalled) InfoItem(R.string.installed, (installedInfo != null).toString())
        if (alwaysInstalled) {
            InfoItem(R.string.source_dir, info.applicationInfo!!.dataDir)
            InfoItem(R.string.data_dir, info.applicationInfo!!.sourceDir)
            InfoItem(R.string.uid, info.applicationInfo!!.uid.toString())
        }
        installedInfo?.let {
            InfoItem(R.string.source_dir, it.dataDir)
            InfoItem(R.string.data_dir, it.sourceDir)
            InfoItem(R.string.uid, it.uid.toString())
        }
    }
}

@Composable
fun InfoItem(title: Int, summary: String) {
    Row(modifier = Modifier.width(230.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(title))
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