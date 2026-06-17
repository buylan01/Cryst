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

package com.buylan.cryst.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.buylan.cryst.ui.screen.home.model.AudioFileData
import com.buylan.cryst.vfs.ArchiveFile
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

val DefaultPath: VirtualFile = LocalFile("/storage/emulated/0")
const val ExtractPath = "/storage/emulated/0/Cryst/package"
val invalidChars = listOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

@SuppressLint("SdCardPath")
private val VIRTUAL_DIRS_MAP = mapOf(
    "/" to listOf("/data", "/etc" ,"/mnt" ,"/proc", "/product", "/storage", "/system", "/system_ext", "/vendor", "/sdcard"),
    "/storage" to listOf("/storage/emulated"),
    "/storage/emulated" to listOf("/storage/emulated/0")
)

fun accessFiles(path: VirtualFile, sortType: FileSortType): List<VirtualFile> {
    try {
        val files = path.listFiles()?.toList()!!
        return files.sortedWith(
            compareBy<VirtualFile> { !it.isDirectory }
                .then(
                    when (sortType) {
                        FileSortType.NAME -> compareBy { it.name.lowercase() }
                        FileSortType.TYPE -> compareBy { it.extension.lowercase() }
                        FileSortType.SIZE -> compareBy { it.length() }
                        FileSortType.TIME -> compareByDescending { it.lastModified() }
                    }
                )
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return VIRTUAL_DIRS_MAP[path.absolutePath]?.map { LocalFile(it) } ?: emptyList()
    }
}

fun getActualFile(context: Context, f: VirtualFile): VirtualFile {
    if (f is ArchiveFile) {
        val tmpDir = File(File(context.cacheDir.absolutePath + "/archive_cache"), f.entranceFile.hashCode().toString())
        if (!tmpDir.exists()) tmpDir.mkdirs()
        val targetFile = File(tmpDir, f.name)
        f.delegate.getInputStream(f.entry).use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return LocalFile(f,targetFile.absolutePath)
    }
    return LocalFile(f.absolutePath)
}

fun getFileType(file: VirtualFile): FileType {
    return if (file.isDirectory) FileType.FOLDER else FileTypeRegistry.getType(file.extension)
}

fun formatFileSize(sizeInBytes: Long): String {
    return when {
        sizeInBytes < 0x400 -> "$sizeInBytes B"
        sizeInBytes < 0x100000 -> "%.1f KB".format(sizeInBytes / 1024.0)
        sizeInBytes < 0x40000000 -> "%.1f MB".format(sizeInBytes / (1024.0 * 1024.0))
        else -> "%.1f GB".format(sizeInBytes / (1024.0 * 1024.0 * 1024.0))
    }
}

fun getFileSize(file: VirtualFile): String {
    return if (file.isDirectory) {
        "未知"
    } else {
        formatSizeDetail(file.length())
    }
}

fun formatSizeDetail(size: Long): String {
    if (size <= 0) return "0 B"

    val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()

    return String.format(
        Locale.US,
        "%.1f %s (%d B)",
        size / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups],
        size
    )
}

fun formatFileDate(file: VirtualFile, format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val date = Date(file.lastModified())
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(date)
}


fun createFile(directory: VirtualFile, fileName: String): Boolean {
    try {
        val file = directory.resolve(fileName)
        return file.createNewFile()
    } catch (_: IOException) {
        return false
    }
}

fun createFolder(directory: VirtualFile, folderName: String): Boolean {
    try {
        val folder = directory.resolve(folderName)
        return folder.mkdir()
    } catch (_: IOException) {
        return false
    }
}

fun getMimeType(file: VirtualFile): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    return mimeType ?: URLConnection.guessContentTypeFromName(file.name) ?: "*/*"
}

fun Context.shareFile(file: VirtualFile) {
    val mimeType = getMimeType(file)
    val uri = FileProvider.getUriForFile(
        this,
        "${applicationContext.packageName}.fileProvider",
        File(file.absolutePath)
    )

    Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }.also { intent ->
        startActivity(Intent.createChooser(intent, "分享文件"))
    }
}

fun VirtualFile.isRootPath(): Boolean {
    return absolutePath == "/"
}

fun getAudioMetadata(file: String): AudioFileData? {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(file)

        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        var bitmap: Bitmap?

        val pictureData = retriever.embeddedPicture
        bitmap = if (pictureData != null && pictureData.isNotEmpty())
            BitmapFactory.decodeByteArray(pictureData, 0, pictureData.size)
        else
            null

        return AudioFileData(
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            cover = bitmap
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    } finally {
        retriever.release()
    }
}