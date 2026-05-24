package com.buylan.cryst.vfs

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
}