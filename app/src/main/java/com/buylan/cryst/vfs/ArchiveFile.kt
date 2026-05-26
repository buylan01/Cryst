package com.buylan.cryst.vfs

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.IOException

class ArchiveFile(
    val entranceFile: VirtualFile,
    val entry: ZipArchiveEntry? = null,
    override val parent: VirtualFile? = entranceFile.parent,
    val entryName: String? = null
) : VirtualFile {
    override val absolutePath = entryName ?: entry?.name ?: ""
    override val name: String = absolutePath.trimEnd('/').split('/').lastOrNull() ?: ""
    override val isDirectory = entry?.isDirectory ?: true
    override val path = absolutePath
    override val pathDisplay: String = entranceFile.name + "/" + path
    override val extension: String = name.substringAfterLast(".")

    override fun listFiles(): List<VirtualFile>? {
        if (!isDirectory) return null

        val dirPath = when {
            absolutePath.isEmpty() -> ""
            !absolutePath.endsWith("/") -> "$absolutePath/"
            else -> absolutePath
        }

        val zipFile = ZipFile(entranceFile.absolutePath)

        try {
            val entries = zipFile.entries.asSequence().toList()
            val childDirNames = mutableSetOf<String>()
            val childFiles = mutableListOf<Pair<ZipArchiveEntry, String>>() // entry, filename

            for (entry in entries) {
                val entryName = entry.name

                // Filter entry in current path
                if (!entryName.startsWith(dirPath) || entryName == dirPath) continue
                val subStr = entryName.removePrefix(dirPath)
                if (subStr.isEmpty()) continue

                val parts = subStr.split('/', limit = 2)
                if (parts.size > 1) {
                    // normal style
                    childDirNames.add(parts[0])
                } else {
                    // file
                    childFiles.add(entry to parts[0])
                }
            }

            // Generate gone dir
            val childDirs = childDirNames.map { dirName ->
                val folderEntryPath = dirPath + dirName
                ArchiveFile(entranceFile, null, this, folderEntryPath).apply {
                }
            }
            // normal file
            val childFileNodes = childFiles.map { (entry, _) ->
                ArchiveFile(entranceFile, entry, this)
            }

            return childDirs + childFileNodes

        } catch (_: IOException) {
            return null
        }
    }

    override fun readBytes(): ByteArray? = if (!isDirectory && entry != null) ZipFile(entranceFile.toFile()).getInputStream(entry).readBytes() else null
    override fun length(): Long = entry?.size ?: 0L
    override fun lastModified(): Long = entry?.lastModifiedTime?.toMillis() ?: 0L
    override fun resolve(fileName: String): VirtualFile {
        val zipFile = ZipFile(entranceFile.absolutePath)
        if (!isDirectory) throw UnsupportedOperationException("Not a directory")
        val childPath = when {
            absolutePath.isEmpty() -> fileName
            !absolutePath.endsWith("/") -> "$absolutePath/$fileName"
            else -> absolutePath + fileName
        }
        val entry = zipFile.getEntry(childPath)
        return ArchiveFile(entranceFile, entry, this)
    }
    override fun createNewFile(): Boolean = false
    override fun mkdir(): Boolean = false
    override fun renameTo(targetFile: VirtualFile): Boolean = false
    override fun exists(): Boolean = true
    override fun walkTopDown(): List<VirtualFile> {
        val result = mutableListOf<VirtualFile>()
        val zipFile = ZipFile(entranceFile.absolutePath)
        result.add(this)
        if (isDirectory) {
            val entries = zipFile.entries.asSequence().toList()
            for (child in entries) {
                if (!child.name.startsWith(name) || entryName == child.name) continue
                result.add(ArchiveFile(entranceFile, child, this))
            }
        }
        return result
    }

    override fun relativeTo(source: VirtualFile): String {
        val sourcePath = (source as? ArchiveFile)?.absolutePath ?: source.path
        val prefix = if (sourcePath.endsWith("/")) sourcePath else "$sourcePath/"
        return if (this.absolutePath.startsWith(prefix)) {
            this.absolutePath.removePrefix(prefix)
        } else {
            this.absolutePath
        }
    }

    override fun mkdirs() {
        TODO("Not yet implemented")
    }

    override fun copyTo(target: LocalFile, overwrite: Boolean) {
        if (this.isDirectory) throw UnsupportedOperationException("Cannot copy directories from archive.")
        if (!overwrite && target.exists()) throw IOException("Target exists: $target")
        val bytes = this.readBytes() ?: throw IOException("Failed to read archive entry: $absolutePath")
        target.toFile().outputStream().use { it.write(bytes) }
    }
}