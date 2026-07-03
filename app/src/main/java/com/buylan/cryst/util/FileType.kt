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

import com.buylan.cryst.R

enum class FileType(
    val label: Int,
    val icon: Int
) {
    FOLDER(R.string.folder, R.drawable.ic_folder),
    FILE(R.string.file, R.drawable.ic_draft),
    TEXT(R.string.text, R.drawable.ic_description),
    AUDIO(R.string.audio, R.drawable.ic_audio_file),
    IMAGE(R.string.image, R.drawable.ic_image),
    VIDEO(R.string.video, R.drawable.ic_video_file),
    ARCHIVE(R.string.archive, R.drawable.ic_folder_zip),
    APK(R.string.installable, R.drawable.ic_apk_document),
    SCRIPT(R.string.script, R.drawable.ic_terminal_2),
    FONT(R.string.font, R.drawable.ic_font_download),
    BYTES(R.string.bytes, R.drawable.ic_draft)
}