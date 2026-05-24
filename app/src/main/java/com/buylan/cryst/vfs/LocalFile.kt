package com.buylan.cryst.vfs

import java.io.File

class LocalFile(private val delegate: File) : VirtualFile {
    constructor(path: String, parent: VirtualFile? = null) : this(File(path))
    constructor(path: String) : this(File(path))

    override val name: String = delegate.name
    override val parent: LocalFile?
        get() = delegate.parentFile?.takeIf { it != delegate }?.let { LocalFile(it) }
    override val isDirectory = delegate.isDirectory
    override val absolutePath: String = delegate.absolutePath
    override val path: String = delegate.path
    override val pathDisplay: String = absolutePath
    override val extension = delegate.extension

    override fun listFiles(): List<VirtualFile>? {
        return delegate.listFiles()?.map { LocalFile(it) }
    }
    override fun readBytes(): ByteArray? = if (!isDirectory) delegate.readBytes() else null
    override fun length(): Long = delegate.length()

    override fun lastModified(): Long = delegate.lastModified()
}