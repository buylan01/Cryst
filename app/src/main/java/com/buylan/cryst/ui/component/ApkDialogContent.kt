package com.buylan.cryst.ui.component

import android.content.pm.PackageInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ApkDialogScaffold(
    app: PackageInfo,
    fromFile: Boolean,
    menu: @Composable () -> Unit = {  }
) {
    val context = LocalContext.current
    val pm = context.packageManager
    Column(
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            val icon = remember {
                try {
                    pm.getApplicationIcon(app.packageName)
                } catch (_: Exception) {
                    null
                }
            }

            AsyncImage(
                model = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pm.getApplicationLabel(app.applicationInfo!!).toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.versionName.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            menu()
        }
        HorizontalDivider(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp))
        ApkInfoColumn(app, !fromFile,if (fromFile) null else app.applicationInfo)
    }
}