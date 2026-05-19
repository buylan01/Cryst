package com.prolan.catu.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.prolan.catu.ui.screen.home.model.FileType
import com.prolan.catu.ui.screen.home.model.SortType
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.math.log10
import kotlin.math.pow

const val RootPath = "/storage/emulated/0"
const val ExtractPath = "${RootPath}/Catu/Extract"
val invalidChars = listOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

fun accessFiles(path: Path, sortType: SortType): List<File> {
    try {
        val files = path.toFile().listFiles()?.toList()!!

        return files.sortedWith(
            compareBy<File> { !it.isDirectory }
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

fun getFileType(file: File): FileType {
    return if (file.isDirectory) FileType.FOLDER else when (file.extension.lowercase()) {
        "txt", "xml", "prop", "conf", "json", "smali" -> FileType.TEXT
        "jpg", "jpeg", "png", "gif", "webp" -> FileType.IMAGE
        "mp3", "wav", "ogg", "flac" -> FileType.AUDIO
        "mp4" -> FileType.VIDEO
        "sh", "rc" -> FileType.SCRIPT
        "ttf", "otf" -> FileType.FONT
        "apk" -> FileType.INSTALLABLE
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

fun getFileSize(file: File): String {
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

fun formatFileDate(file: File): String {
    val date = Date(file.lastModified())
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
}


fun createFile(directory: Path, fileName: String): Boolean {
    try {
        val file = directory.resolve(fileName).toFile()
        return file.createNewFile()
    } catch (_: IOException) {
        return false
    }
}

fun renameFile(file: File, targetFile: File): Boolean {
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

fun moveFile(file: File, targetFile: File): Boolean {
    try {
        Files.move(Path(file.path), Path(targetFile.path))
        return true
    } catch (_: IOException) {
        return false
    }
}

fun createFolder(directory: Path, folderName: String): Boolean {
    try {
        val folder = directory.resolve(folderName).toFile()
        return folder.mkdir()
    } catch (_: IOException) {
        return false
    }
}

fun getMimeType(file: File): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    return mimeType ?: URLConnection.guessContentTypeFromName(file.name) ?: "*/*"
}

fun Context.shareFile(file: File) {
    val mimeType = getMimeType(file)
    val uri = FileProvider.getUriForFile(
        this,
        "${applicationContext.packageName}.fileProvider",
        file
    )

    Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }.also { intent ->
        startActivity(Intent.createChooser(intent, "分享文件"))
    }
}

fun install(context: Context, file: File) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileProvider",
            file
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

fun Path.isRootPath(): Boolean {
    return pathString == "/storage/emulated/0"
}