package com.buylan.cryst.util

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.collections.forEach

fun createZip(
    files: List<File>,
    outputZipFile: File,
    baseDir: File
): File {
    ZipArchiveOutputStream(outputZipFile).use { zipOut ->
        files.forEach { file ->
            val relativePath = file.relativeTo(baseDir).path.replace("\\", "/")
            val entry = ZipArchiveEntry(file, relativePath)
            zipOut.putArchiveEntry(entry)
            FileInputStream(file).use { fis ->
                fis.copyTo(zipOut)
            }
            zipOut.closeArchiveEntry()
        }
    }
    return outputZipFile
}

fun createTar(
    files: List<File>,
    outputTarFile: File,
    baseDir: File
): File {
    FileOutputStream(outputTarFile).use { fos ->
        TarArchiveOutputStream(fos).use { zipOut ->
            files.forEach { file ->
                val relativePath = file.relativeTo(baseDir).path.replace("\\", "/")
                val entry = TarArchiveEntry(file, relativePath)
                zipOut.putArchiveEntry(entry)
                FileInputStream(file).use { fis ->
                    fis.copyTo(zipOut)
                }
                zipOut.closeArchiveEntry()
            }
        }
    }
    return outputTarFile
}