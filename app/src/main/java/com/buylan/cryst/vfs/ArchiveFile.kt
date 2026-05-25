package com.buylan.cryst.vfs

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import java.io.IOException

class ArchiveFile(
    val entranceFile: VirtualFile,
    val entry: ZipArchiveEntry? = null,
    override val parent: VirtualFile? = entranceFile.parent
) : VirtualFile {
    override val name: String
        get() {
            val n = entry?.name ?: return ""
            val parts = n.trimEnd('/').split('/')
            return parts.lastOrNull() ?: ""
        }
    override val isDirectory = entry?.isDirectory ?: true
    override val absolutePath = entry?.name ?: ""
    override val path = entry?.name ?: ""
    override val pathDisplay: String = entranceFile.name + "/" + path
    override val extension: String = entry?.name?.substringAfterLast(".") ?: ""

    override fun listFiles(): List<VirtualFile>? {
        if (!isDirectory) return null

        val dirPath = when {
            absolutePath.isEmpty() -> ""
            !absolutePath.endsWith("/") -> "$absolutePath/"
            else -> absolutePath
        }

        val zipFile = ZipFile(entranceFile.absolutePath)

        try {
            val children = zipFile.entries.asSequence()
                .filter { entry ->
                    val entryName = entry.name
                    if (!entryName.startsWith(dirPath) || entryName == dirPath) return@filter false // 排除自己
                    val substr = entryName.removePrefix(dirPath)
                    !substr.isEmpty() && !substr.dropLast(1).contains('/')
                }
                .map { ArchiveFile(entranceFile, it, this) }
                .toList()

            return children
        } catch (e: IOException) {
            return null
        }
    }
    override fun readBytes(): ByteArray? =
        if (!isDirectory && entry != null) ZipFile(entranceFile.toFile()).getInputStream(entry).readBytes() else null

    override fun length(): Long {
        return entry?.size ?: 0L
    }
    override fun lastModified(): Long {
        return entry?.lastModifiedTime?.toMillis() ?: 0L
    }

    override fun resolve(fileName: String): VirtualFile {
        // 在当前目录内查找某个 entry
        val zipFile = ZipFile(entranceFile.absolutePath)
        if (!isDirectory) throw UnsupportedOperationException("Not a directory")
        val childPath = when {
            absolutePath.isEmpty() -> fileName     // zip根
            !absolutePath.endsWith("/") -> "$absolutePath/$fileName"
            else -> absolutePath + fileName
        }
        val entry = zipFile.getEntry(childPath)
        // 未找到直接返回 null，或者返回不存在的 ArchiveFile（此处返回表示虚拟文件，未必可用）
        return ArchiveFile(entranceFile, entry, this)
    }


    override fun createNewFile(): Boolean = false

    override fun mkdir(): Boolean = false

    override fun renameTo(targetFile: VirtualFile): Boolean = false
}