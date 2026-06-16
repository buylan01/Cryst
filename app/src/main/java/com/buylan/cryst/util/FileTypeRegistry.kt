package com.buylan.cryst.util

import com.buylan.cryst.ui.screen.home.model.FileType

object FileTypeRegistry {
    private val extensionMap = mutableMapOf<String, FileType>()

    init {
        addAll(listOf("txt", "xml", "prop", "conf", "json", "smali", "cpp", "html", "bat", "log"), FileType.TEXT)
        addAll(listOf("jpg", "jpeg", "png", "gif", "webp", "heif"), FileType.IMAGE)
        addAll(listOf("mp3", "wav", "ogg", "flac"), FileType.AUDIO)
        addAll(listOf("mp4"), FileType.VIDEO)
        addAll(listOf("sh", "rc"), FileType.SCRIPT)
        addAll(listOf("ttf", "otf"), FileType.FONT)
        addAll(listOf("apk", "apex"), FileType.APK)
        addAll(listOf("zip", "rar", "7z"), FileType.ARCHIVE)
    }

    fun add(extension: String, type: FileType) {
        extensionMap[extension.lowercase()] = type
    }

    fun addAll(extensions: List<String>, type: FileType) {
        extensions.forEach { add(it, type) }
    }

    fun getType(extension: String): FileType {
        return extensionMap[extension.lowercase()] ?: FileType.FILE
    }
}