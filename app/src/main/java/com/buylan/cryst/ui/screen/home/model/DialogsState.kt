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

package com.buylan.cryst.ui.screen.home.model

import android.os.Environment
import com.buylan.cryst.vfs.VirtualFile

data class DialogsState(
    val pathDialog: Boolean = false,
    val aboutDialog: Boolean = false,
    val sortDialog: Boolean = false,
    val permissionRequest: Boolean = !Environment.isExternalStorageManager(),

    val apkDialog: VirtualFile? = null,
    val renameDialog: VirtualFile? = null,
    val createDialog: VirtualFile? = null,
    val searchDialog: VirtualFile? = null,
    val audioDialog: VirtualFile? = null,
    val openWithDialog: VirtualFile? = null,
    val runScriptDialog: VirtualFile? = null,

    val propertiesDialog: List<VirtualFile>? = null,
    val deleteDialog: List<VirtualFile>? = null,
    val compressDialog: List<VirtualFile>? = null,
    val toolsDialog: List<VirtualFile>? = null,

    val copyDialog: ExtraDialogState? = null,
    val moveDialog: ExtraDialogState? = null
)