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

package com.buylan.cryst.vfs

import java.io.File

interface VirtualFile {
    val name: String
    val path: String
    val pathDisplay: String
    val isDirectory: Boolean
    val parent: String?
    val parentFile: VirtualFile?
    val absolutePath: String
    val extension: String

    fun listFiles(): List<VirtualFile>?
    fun readBytes(): ByteArray?
    fun length(): Long
    fun lastModified(): Long
    fun resolve(fileName: String): VirtualFile
    fun createNewFile(): Boolean
    fun renameTo(targetFile: VirtualFile): Boolean
    fun mkdir(): Boolean
    fun toFile(): File = File(this.absolutePath)
    fun exists(): Boolean
    fun walkTopDown(): List<VirtualFile>
    fun walkTopDownSequence(): Sequence<VirtualFile>
    fun relativeTo(source: VirtualFile): String
    fun mkdirs()
    fun copyTo(target: LocalFile, overwrite: Boolean)
}