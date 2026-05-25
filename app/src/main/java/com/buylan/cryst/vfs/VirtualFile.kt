package com.buylan.cryst.vfs

import java.io.File

interface VirtualFile {
    val name: String
    val path: String
    val pathDisplay: String
    val isDirectory: Boolean
    val parent: VirtualFile?
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
}