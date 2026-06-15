package com.buylan.cryst.vfs

import java.io.File
import java.io.IOException

class LocalFile(
    private val delegate: File,
) : VirtualFile {
    constructor(path: String) : this(File(path))
    constructor(parentFile: VirtualFile, child: String) : this(child) { this._parentFile = parentFile }
    constructor(parent: String?, child: String) : this(parent?.let { File(it, child) } ?: File(child))

    private var _parentFile: VirtualFile? = null

    override val name: String
        get() = delegate.name
    override val parent: String?
        get() = delegate.parent
    override val parentFile: VirtualFile?
        get() = _parentFile ?: delegate.parentFile?.let { LocalFile(it) }
    override val isDirectory
        get() = delegate.isDirectory
    override val absolutePath: String
        get() = delegate.absolutePath
    override val path: String
        get() = delegate.path
    override val pathDisplay: String
        get() = absolutePath
    override val extension
        get() =  delegate.extension

    override fun listFiles(): List<VirtualFile>? = delegate.listFiles()?.map { LocalFile(it) }
    override fun readBytes(): ByteArray? = if (!isDirectory) delegate.readBytes() else null
    override fun length(): Long = delegate.length()
    override fun lastModified(): Long = delegate.lastModified()
    override fun resolve(fileName: String): VirtualFile = LocalFile(delegate.resolve(fileName).absolutePath)
    override fun createNewFile(): Boolean = delegate.createNewFile()
    override fun mkdir(): Boolean = delegate.mkdir()
    override fun renameTo(targetFile: VirtualFile): Boolean {
        return try {
            delegate.renameTo(targetFile.toFile())
            true
        } catch (_: IOException) {
            false
        }
    }
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

    override fun walkTopDownSequence(): Sequence<VirtualFile> {
        val self = this
        return sequence {
            yield(self)
            if (isDirectory) {
                listFiles()?.forEach { child ->
                    yieldAll(child.walkTopDownSequence())
                }
            }
        }
    }

    override fun relativeTo(source: VirtualFile): String = delegate.relativeTo(source.toFile()).absolutePath
    override fun copyTo(target: LocalFile, overwrite: Boolean) {
        delegate.copyTo(target.delegate, overwrite)
    }
    override fun mkdirs() {
        delegate.mkdirs()
    }
}