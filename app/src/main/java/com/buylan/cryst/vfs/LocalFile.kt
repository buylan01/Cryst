package com.buylan.cryst.vfs

import android.annotation.SuppressLint
import java.io.File

class LocalFile(
    private val delegate: File,
    override val parent: VirtualFile? = delegate.parentFile?.takeIf { it != delegate }?.let { LocalFile(it) }
) : VirtualFile {
    constructor(parent: String?, child: String) : this(parent?.let { File(it, child) } ?: File(child))
    constructor(parent: VirtualFile? = null, path: String) : this(File(path), parent = parent)
    constructor(path: String) : this(File(path))

    override val name: String = delegate.name
    override val isDirectory = delegate.isDirectory
    override val absolutePath: String = delegate.absolutePath
    override var path: String = delegate.path
    override val pathDisplay: String = absolutePath
    override val extension = delegate.extension

    override fun listFiles(): List<VirtualFile>? = delegate.listFiles()?.map { LocalFile(it) }
    override fun readBytes(): ByteArray? = if (!isDirectory) delegate.readBytes() else null
    override fun length(): Long = delegate.length()
    override fun lastModified(): Long = delegate.lastModified()
    override fun resolve(fileName: String): VirtualFile = LocalFile(delegate.resolve(fileName).absolutePath)
    override fun createNewFile(): Boolean = delegate.createNewFile()
    override fun mkdir(): Boolean = delegate.mkdir()
    override fun renameTo(targetFile: VirtualFile): Boolean = delegate.renameTo(File(targetFile.absolutePath))
    override fun exists(): Boolean = delegate.exists()
    override fun walkTopDown(): List<VirtualFile> {
        val result = mutableListOf<VirtualFile>()
        result.add(this)
        if (isDirectory) {
            listFiles()?.forEach { child ->
                result.addAll(child.walkTopDown())
            }
        }
        return result
    }
    override fun relativeTo(source: VirtualFile): String = delegate.relativeTo(source.toFile()).absolutePath
    override fun copyTo(target: LocalFile, overwrite: Boolean) {
        delegate.copyTo(target.delegate, overwrite)
    }
    override fun mkdirs() {
        delegate.mkdirs()
    }
}