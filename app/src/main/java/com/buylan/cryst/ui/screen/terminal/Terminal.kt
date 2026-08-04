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

package com.buylan.cryst.ui.screen.terminal

import android.os.Environment
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import com.buylan.cryst.R
import com.termux.terminal.TerminalSession
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Terminal(
    scriptPath: String?
) {
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.terminal), maxLines = 1, softWrap = false, overflow = TextOverflow.StartEllipsis) },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as ComponentActivity).finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {  }) {
                        Icon(painter = painterResource(R.drawable.ic_more_vert), null)
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.displayCutout
    ) { contentPadding ->
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    val view = TerminalView(ctx, null)
                    val sessionClient =
                        object : com.termux.terminal.TerminalSessionClient {
                            override fun onTextChanged(session: TerminalSession) {
                                view.onScreenUpdated()
                            }

                            override fun onTitleChanged(session: TerminalSession) {}
                            override fun onSessionFinished(session: TerminalSession) {}
                            override fun onCopyTextToClipboard(
                                session: TerminalSession?,
                                text: String?
                            ) {
                            }

                            override fun onPasteTextFromClipboard(session: TerminalSession?) {}
                            override fun onBell(session: TerminalSession) {}
                            override fun onColorsChanged(session: TerminalSession) {
                            }
                            override fun onTerminalCursorStateChange(state: Boolean) {}
                            override fun getTerminalCursorStyle(): Int? {
                                return null
                            }

                            override fun logError(tag: String, message: String) {}
                            override fun logWarn(tag: String, message: String) {}
                            override fun logInfo(tag: String, message: String) {}
                            override fun logDebug(tag: String, message: String) {}
                            override fun logVerbose(tag: String, message: String) {}
                            override fun logStackTraceWithMessage(
                                tag: String,
                                message: String,
                                e: Exception
                            ) {
                            }

                            override fun logStackTrace(tag: String, e: Exception) {}
                        }

                    val envs = mutableListOf("/bin")
                    val args = scriptPath?.let { arrayOf("-c", scriptPath) } ?: arrayOf()

                    val session = TerminalSession(
                        "/system/bin/sh",
                        Environment.getExternalStorageDirectory().path,
                        args,
                        envs.toTypedArray(),
                        2000,
                        sessionClient
                    )

                    view.setTerminalViewClient(object : TerminalViewClient {
                        override fun onScale(scale: Float): Float = scale
                        override fun onSingleTapUp(e: MotionEvent?) {
                            view.requestFocus()
                            view.windowInsetsController?.show(android.view.WindowInsets.Type.ime())
                        }

                        override fun shouldBackButtonBeMappedToEscape(): Boolean = false
                        override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
                        override fun isTerminalViewSelected(): Boolean = true
                        override fun copyModeChanged(copyMode: Boolean) {}
                        override fun onKeyDown(
                            keyCode: Int,
                            e: KeyEvent?,
                            session: TerminalSession?
                        ): Boolean {
                            return false
                        }

                        override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
                            return false
                        }

                        override fun onLongPress(event: MotionEvent?): Boolean = false
                        override fun readControlKey(): Boolean = false
                        override fun readAltKey(): Boolean = false
                        override fun readShiftKey(): Boolean = false
                        override fun readFnKey(): Boolean = false
                        override fun onCodePoint(
                            codePoint: Int,
                            ctrlDown: Boolean,
                            session: TerminalSession?
                        ): Boolean {
                            return false
                        }

                        override fun shouldEnforceCharBasedInput(): Boolean = false
                        override fun onEmulatorSet() {}
                        override fun logError(tag: String?, message: String?) {}
                        override fun logWarn(tag: String?, message: String?) {}
                        override fun logInfo(tag: String?, message: String?) {}
                        override fun logDebug(tag: String?, message: String?) {}
                        override fun logVerbose(tag: String?, message: String?) {}
                        override fun logStackTraceWithMessage(
                            tag: String?,
                            message: String?,
                            e: Exception?
                        ) {
                        }

                        override fun logStackTrace(tag: String?, e: Exception?) {}
                    })
                    view.setTextSize(48)
                    view.isFocusableInTouchMode = true
                    view.attachSession(session)
                    view
                },
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            )
        }
    }
}