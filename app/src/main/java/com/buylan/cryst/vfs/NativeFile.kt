package com.buylan.cryst.vfs

import com.buylan.cryst.util.ShellExecutor
import java.io.File

class NativeFile(
    override val path: String,
    private val cachedIsDir: Boolean = true,
    private val cachedSize: Long? = null,
    private val cachedTime: Long? = null
) : VirtualFile {

    override val name: String = File(path).name

    override val absolutePath: String = path

    override val isDirectory: Boolean
        get() = cachedIsDir

    override val parent: String?
        get() = File(path).parent

    override val parentFile: VirtualFile?
        get() = parent?.let { NativeFile(it) }

    override val extension: String
        get() = File(path).extension

    override val pathDisplay: String
        get() = path

    override fun listFiles(): List<VirtualFile>? {
        return try {
            val cmd = "$ShellExecutor \"stat -c '%n|%F|%s|%Y' '$path'/* '$path'/.* 2>/dev/null\""
            val result = exec(cmd)
            result.lineSequence()
                .filter { it.isNotBlank() }
                .mapNotNull { parseStatLine(it) }
                .toList()

        } catch (e: Exception) {
            println(e)
            null
        }
    }

    private fun parseStatLine(line: String): VirtualFile? {
        val parts = line.split("|")
        if (parts.size < 4) return null

        val fullPath = parts[0]
        val type = parts[1]
        val size = parts[2]
        val time = parts[3]

        println(type.contains("directory"))
        return NativeFile(fullPath,
            cachedIsDir = type.contains("directory"),
            cachedSize = size.toLong(),
            cachedTime = time.toLong()
        )
    }

    override fun readBytes(): ByteArray? {
        return try {
            val cmd = "$ShellExecutor \"cat \\\"$path\\\"\""
            execBytes(cmd)
        } catch (_: Exception) {
            null
        }
    }

    override fun length(): Long {
        return cachedSize ?: 0L
    }

    override fun lastModified(): Long {
        return cachedTime ?: 0L
    }

    override fun resolve(fileName: String): VirtualFile {
        return NativeFile("$path/$fileName")
    }

    override fun createNewFile(): Boolean {
        return execAndGetCode("$ShellExecutor \"touch \\\"$path\\\"\"") == 0
    }

    override fun mkdir(): Boolean {
        return try {
            exec("$ShellExecutor \"mkdir \\\"$path\\\"\"")
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun mkdirs() {
        exec("$ShellExecutor \"mkdir -p \\\"$path\\\"\"")
    }

    override fun renameTo(targetFile: VirtualFile): Boolean {
        return try {
            exec("$ShellExecutor \"mv \\\"$path\\\" \\\"${targetFile.absolutePath}\\\"\"")
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun exists(): Boolean {
        return exec("$ShellExecutor \"test -e \\\"$path\\\" && echo 1 || echo 0\"").trim() == "1"
    }

    override fun walkTopDown(): List<VirtualFile> {
        return walkTopDownSequence().toList()
    }

    override fun walkTopDownSequence(): Sequence<VirtualFile> = sequence {
        yield(this@NativeFile)
        if (!isDirectory) return@sequence

        val cmd = "$ShellExecutor \"find \\\"$path\\\" -exec stat -c '%n|%F|%s|%Y' {} + 2>/dev/null\""
        val output = exec(cmd)

        output.lineSequence()
            .mapNotNull { parseStatLine(it) }
            .filter { it.absolutePath != path }
            .forEach { yield(it) }
    }

    override fun relativeTo(source: VirtualFile): String {
        return path.removePrefix(source.absolutePath).trimStart('/')
    }

    override fun copyTo(target: LocalFile, overwrite: Boolean) {
        val flag = if (overwrite) "-f" else ""
        exec("$ShellExecutor \"cp $flag \\\"$path\\\" \\\"${target.absolutePath}\\\"\"")
    }

    private fun exec(cmd: String): String {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
        return process.inputStream.bufferedReader().readText().also {
            process.waitFor()
        }
    }

    private fun execBytes(cmd: String): ByteArray {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
        val bytes = process.inputStream.readBytes()
        process.waitFor()
        return bytes
    }

    private fun execAndGetCode(cmd: String): Int {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
        process.inputStream.bufferedReader().readText()
        process.waitFor()
        return process.exitValue()
    }
}