package com.buylan.cryst.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.buylan.cryst.ui.screen.home.model.FileType
import com.buylan.cryst.ui.screen.home.model.SortType
import com.buylan.cryst.vfs.ArchiveFile
import com.buylan.cryst.vfs.LocalFile
import com.buylan.cryst.vfs.VirtualFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.io.path.Path
import kotlin.math.log10
import kotlin.math.pow

const val RootPath = "/storage/emulated/0"
const val ExtractPath = "${RootPath}/Cryst/package"
val invalidChars = listOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

fun accessFiles(path: VirtualFile, sortType: SortType): List<VirtualFile> {
    try {
        val files = path.listFiles()?.toList()!!

        return files.sortedWith(
            compareBy<VirtualFile> { !it.isDirectory }
                .then(
                    when (sortType) {
                        SortType.NAME -> compareBy { it.name.lowercase() }
                        SortType.TYPE -> compareBy { it.extension.lowercase() }
                        SortType.SIZE -> compareBy { it.length() }
                        SortType.TIME -> compareByDescending { it.lastModified() }
                    }
                )
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return emptyList()
    }
}

fun getActualFile(context: Context, f: VirtualFile): VirtualFile {
    if (f is ArchiveFile) {
        val tmpDir = File(File(context.cacheDir.absolutePath + "/archive_cache"), f.entranceFile.hashCode().toString())
        if (!tmpDir.exists()) tmpDir.mkdirs()
        val targetFile = File(tmpDir, f.name)
        ZipFile(f.entranceFile.absolutePath).getInputStream(f.entry).use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return LocalFile(f,targetFile.absolutePath)
    }
    return LocalFile(f.absolutePath)
}

fun getFileType(file: VirtualFile): FileType {
    return if (file.isDirectory) FileType.FOLDER else when (file.extension.lowercase()) {
        "txt", "xml", "prop", "conf", "json", "smali", "cpp", "html", "bat", "log" -> FileType.TEXT
        "jpg", "jpeg", "png", "gif", "webp" -> FileType.IMAGE
        "mp3", "wav", "ogg", "flac" -> FileType.AUDIO
        "mp4" -> FileType.VIDEO
        "sh", "rc" -> FileType.SCRIPT
        "ttf", "otf" -> FileType.FONT
        "apk" -> FileType.APK
        "zip", "rar", "7z" -> FileType.ARCHIVE
        else -> FileType.FILE
    }
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

fun formatFileDate(file: VirtualFile): String {
    val date = Date(file.lastModified())
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
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

fun renameFile(file: VirtualFile, targetFile: VirtualFile): Boolean {
    return try {
        file.renameTo(targetFile)
    } catch (_: IOException) {
        false
    }
}

fun copyFile(file: File, targetFile: File): Boolean {
    try {
        file.copyTo(targetFile)
        return true
    } catch (_: IOException) {
        return false
    }
}

fun moveFile(file: VirtualFile, targetFile: VirtualFile): Boolean {
    try {
        Files.move(Path(file.path), Path(targetFile.path))
        return true
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

fun install(context: Context, file: VirtualFile) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileProvider",
            File(file.absolutePath)
        )

        setDataAndType(uri, "application/vnd.android.package-archive")
    }
    if (!context.packageManager.canRequestPackageInstalls()) {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = "package:${context.packageName}".toUri()
            }
        )
        return
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "未找到安装程序", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "安装失败: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun VirtualFile.isRootPath(): Boolean {
    return absolutePath == "/storage/emulated/0"
}

fun createZip(files: List<File>, outputZipFile: File): File {
    ZipArchiveOutputStream(outputZipFile).use { zipOut ->
        files.forEach { file ->
            val entry = ZipArchiveEntry(file, file.name)
            zipOut.putArchiveEntry(entry)
            FileInputStream(file).use { fis ->
                IOUtils.copy(fis, zipOut)
            }
            zipOut.closeArchiveEntry()
        }
    }
    return outputZipFile
}

fun createTar(files: List<File>, outputTarFile: File): File {
    FileOutputStream(outputTarFile).use { fos ->
        TarArchiveOutputStream(fos).use { zipOut ->
            files.forEach { file ->
                val entry = TarArchiveEntry(file, file.name)
                zipOut.putArchiveEntry(entry)
                FileInputStream(file).use { fis ->
                    IOUtils.copy(fis, zipOut)
                }
                zipOut.closeArchiveEntry()
            }
        }
    }
    return outputTarFile
}
