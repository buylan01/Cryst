package com.buylan.cryst.ui.screen.home.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buylan.cryst.R
import com.buylan.cryst.ui.screen.home.dialog.ArchiveFormat
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CompressViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<FileOperaUiState>(FileOperaUiState.Idle)
    val uiState: StateFlow<FileOperaUiState> = _uiState.asStateFlow()

    fun startCompress(format: ArchiveFormat, source: List<VirtualFile>, targetDir: VirtualFile, level: Int = 0) {
        viewModelScope.launch {
            _uiState.value = FileOperaUiState.InProgress
            withContext(Dispatchers.IO) {
                try {
                    var current = 0
                    var failedCount = 0
                    val allFiles = source.flatMap { item ->
                        val ioFile = item.toFile()
                        if (item.isDirectory) {
                            ioFile.walkTopDown().toList()
                        } else {
                            listOf(ioFile)
                        }
                    }
                    val total = allFiles.size

                    val baseDir = if (source.isNotEmpty()) {
                        val parents = source.map { it.parentFile }.toSet()
                        if (parents.size == 1) parents.first()!!.toFile()
                        else source.first().parentFile!!.toFile()
                    } else {
                        error("No source files selected")
                    }
                    val outputFile = File(baseDir, targetDir.name)

                    when(format) {
                        ArchiveFormat.ZIP -> {
                            ZipArchiveOutputStream(outputFile).use { zipOut ->
                                zipOut.setLevel(level)
                                allFiles.forEach { item ->
                                    val relativePath =
                                        item.relativeTo(baseDir).path.replace("\\", "/")
                                    when {
                                        item.isDirectory -> {
                                            val entry = ZipArchiveEntry("$relativePath/")
                                            zipOut.putArchiveEntry(entry)
                                            zipOut.closeArchiveEntry()
                                        }

                                        item.isFile -> {
                                            val entry = ZipArchiveEntry(item, relativePath)
                                            zipOut.putArchiveEntry(entry)
                                            FileInputStream(item).use { fis ->
                                                fis.copyTo(zipOut)
                                            }
                                            zipOut.closeArchiveEntry()
                                        }
                                    }
                                    current++
                                    _uiState.value = FileOperaUiState.Progress(
                                        current = current,
                                        total = total,
                                        percentage = (current * 100 / total),
                                        failed = failedCount
                                    )
                                }
                            }
                        }
                        ArchiveFormat.TAR -> {
                            FileOutputStream(outputFile).use { fos ->
                                TarArchiveOutputStream(fos).use { tarOut ->
                                    allFiles.forEach { item ->
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
                                        current++
                                        _uiState.value = FileOperaUiState.Progress(
                                            current = current,
                                            total = total,
                                            percentage = (current * 100 / total),
                                            failed = failedCount
                                        )
                                    }
                                }
                            }
                        }
                    }
                    _uiState.value = FileOperaUiState.Success(true)
                } catch (e: Exception) {
                    _uiState.value = FileOperaUiState.Error(R.string.copy_to_failed)
                }
            }
        }
    }

    fun finish() {
        _uiState.value = FileOperaUiState.Idle
    }
}