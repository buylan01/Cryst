package com.buylan.cryst.vfs

import java.io.File

class LocalFile(
    private val delegate: File,
    override val parent: VirtualFile? = delegate.parentFile?.takeIf { it != delegate }
        ?.let { LocalFile(it) }
) : VirtualFile {
    constructor(path: String, parent: VirtualFile? = null) : this(File(path), parent = parent)
    constructor(path: String) : this(File(path))

    override val name: String = delegate.name
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
    override fun resolve(fileName: String): VirtualFile = LocalFile(delegate.resolve(fileName).absolutePath)

    override fun createNewFile(): Boolean = delegate.createNewFile()

    override fun mkdir(): Boolean = delegate.mkdir()

    override fun renameTo(targetFile: VirtualFile): Boolean = delegate.renameTo(File(targetFile.absolutePath))
}