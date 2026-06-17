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