package com.buylan.cryst.util

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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

    require(files.none { it.absolutePath == outputTarFile.absolutePath }) {
        "Output tar file must not be in the input list"
    }

    FileOutputStream(outputTarFile).use { fos ->
        TarArchiveOutputStream(fos).use { tarOut ->
            files.forEach { item ->
                val relativePath = item.relativeTo(baseDir).path.replace("\\", "/")
                when {
                    item.isDirectory -> {
                        val entry = TarArchiveEntry("$relativePath/")
                        tarOut.putArchiveEntry(entry)
                        tarOut.closeArchiveEntry()
                    }
                    item.isFile -> {
                        val entry = TarArchiveEntry(item, relativePath)
                        tarOut.putArchiveEntry(entry)
                        try {
                            FileInputStream(item).buffered().use { it.copyTo(tarOut) }
                        } finally {
                            tarOut.closeArchiveEntry()
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }
    return outputTarFile
}