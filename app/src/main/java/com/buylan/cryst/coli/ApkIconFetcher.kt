package com.buylan.cryst.coli

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.disk.DiskCache
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import okio.Buffer
import okio.FileSystem
import java.io.ByteArrayOutputStream
import java.io.File

class ApkIconFetcher(
    private val request: ApkIconRequest,
    private val packageManager: PackageManager,
    private val diskCache: DiskCache?,
) : Fetcher {

    private val cacheKey = "${request.apkPath}-${request.lastModified}"

    override suspend fun fetch(): FetchResult {

        diskCache?.openSnapshot(cacheKey)?.use { snapshot ->
            return SourceFetchResult(
                source = ImageSource(snapshot.data, diskCache.fileSystem),
                mimeType = "image/png",
                dataSource = DataSource.DISK
            )
        }

        val info = packageManager.getPackageArchiveInfo(request.apkPath, 0)
            ?: throw Exception("Failed to parse APK")
        info.applicationInfo?.apply {
            sourceDir = request.apkPath
            publicSourceDir = request.apkPath
        } ?: throw Exception("No application info")
        val bitmap = info.applicationInfo!!.loadIcon(packageManager).toBitmap()
        val bytes = ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }

        diskCache?.openEditor(cacheKey)?.let { editor ->
            diskCache.fileSystem.write(editor.data) { write(bytes) }
            editor.commit()
        }

        diskCache?.openSnapshot(cacheKey)?.use { snapshot ->
            return SourceFetchResult(
                source = ImageSource(snapshot.data, diskCache.fileSystem),
                mimeType = "image/png",
                dataSource = DataSource.DISK
            )
        }

        return SourceFetchResult(
            source = ImageSource(Buffer().write(bytes), FileSystem.SYSTEM),
            mimeType = "image/png",
            dataSource = DataSource.MEMORY
        )
    }

    class Factory(private val packageManager: PackageManager) : Fetcher.Factory<ApkIconRequest> {
        override fun create(data: ApkIconRequest, options: Options, imageLoader: ImageLoader): Fetcher {
            return ApkIconFetcher(data, packageManager, imageLoader.diskCache)
        }
    }
}

data class ApkIconRequest(
    val apkPath: String,
    val lastModified: Long = File(apkPath).lastModified()
)