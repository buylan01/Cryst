package com.buylan.cryst.ui.screen.home.dialog

import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.buylan.cryst.R
import com.buylan.cryst.activity.TerminalActivity
import com.buylan.cryst.vfs.VirtualFile

@Composable
fun ScriptDialog(
    onDismiss: () -> Unit,
    target: VirtualFile
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.script))
        },
        text = {
            Text(stringResource(R.string.script_run_text, target.name))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    context.startActivity(
                        Intent(context, TerminalActivity::class.java)
                    )
                }
            ) {
                Text(stringResource(R.string.run))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    stringResource(R.string.cancel)
                )
            }
        }
    )
}