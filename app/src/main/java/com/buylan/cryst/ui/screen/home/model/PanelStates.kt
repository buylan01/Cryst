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

import com.buylan.cryst.util.DefaultPath
import com.buylan.cryst.util.FileSortType
import com.buylan.cryst.vfs.VirtualFile

data class PanelStates(
    val path: VirtualFile = DefaultPath,
    val history: List<VirtualFile> = listOf(),
    val historyIndex: Int = 0,
    val highLightFiles: Set<String> = emptySet(),
    val selectedFiles: Set<String> = emptySet(),
    val files: List<VirtualFile> = emptyList(),
    val sortType: FileSortType = FileSortType.NAME,
    val rangeAnchorPath: String? = null
)