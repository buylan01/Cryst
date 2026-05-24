package com.buylan.cryst.vfs

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import java.io.IOException

class ArchiveFile(
    private val zipFile: ZipFile,
    private val entry: ZipArchiveEntry? = null,
    private val zipFilePath: File? = null,
    override val parent: VirtualFile? = zipFilePath?.let { LocalFile(zipFilePath.parentFile) }
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
    override val pathDisplay: String = zipFilePath?.name + "/" + path
    override val extension: String = entry?.name?.substringAfterLast(".") ?: ""

    override fun listFiles(): List<VirtualFile>? {
        if (!isDirectory) return null

        val dirPath = when {
            absolutePath.isEmpty() -> ""
            !absolutePath.endsWith("/") -> absolutePath + "/"
            else -> absolutePath
        }

        try {
            val children = zipFile.entries.asSequence()
                .filter { entry ->
                    val entryName = entry.name
                    if (!entryName.startsWith(dirPath) || entryName == dirPath) return@filter false // 排除自己
                    val substr = entryName.removePrefix(dirPath)
                    !substr.isEmpty() && !substr.dropLast(1).contains('/')
                }
                .map { ArchiveFile(zipFile, it, zipFilePath, this) }
                .toList()

            return children
        } catch (e: IOException) {
            return null
        }
    }
    override fun readBytes(): ByteArray? =
        if (!isDirectory && entry != null) zipFile.getInputStream(entry).readBytes() else null

    override fun length(): Long {
        return entry?.size ?: 0L
    }
    override fun lastModified(): Long {
        return entry?.lastModifiedTime?.toMillis() ?: 0L
    }
}