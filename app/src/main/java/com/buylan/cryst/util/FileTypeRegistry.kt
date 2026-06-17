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

package com.buylan.cryst.util

import com.buylan.cryst.ui.screen.home.model.FileType

object FileTypeRegistry {
    private val extensionMap = mutableMapOf<String, FileType>()

    init {
        addAll(listOf("txt", "xml", "prop", "conf", "json", "smali", "cpp", "html", "bat", "log"), FileType.TEXT)
        addAll(listOf("jpg", "jpeg", "png", "gif", "webp", "heif"), FileType.IMAGE)
        addAll(listOf("mp3", "wav", "ogg", "flac"), FileType.AUDIO)
        addAll(listOf("mp4"), FileType.VIDEO)
        addAll(listOf("sh", "rc"), FileType.SCRIPT)
        addAll(listOf("ttf", "otf"), FileType.FONT)
        addAll(listOf("apk", "apex"), FileType.APK)
        addAll(listOf("zip", "rar", "7z"), FileType.ARCHIVE)
    }

    fun add(extension: String, type: FileType) {
        extensionMap[extension.lowercase()] = type
    }

    fun addAll(extensions: List<String>, type: FileType) {
        extensions.forEach { add(it, type) }
    }

    fun getType(extension: String): FileType {
        return extensionMap[extension.lowercase()] ?: FileType.FILE
    }
}