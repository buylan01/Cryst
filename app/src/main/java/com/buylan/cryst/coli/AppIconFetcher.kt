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
import kotlin.use

class AppIconFetcher(
    private val request: AppIconRequest,
    private val packageManager: PackageManager,
    private val diskCache: DiskCache?,
) : Fetcher {

    private val cacheKey = "${request.packageName}-${request.lastUpdate}"

    override suspend fun fetch(): FetchResult {

        diskCache?.openSnapshot(cacheKey)?.use { snapshot ->
            return SourceFetchResult(
                source = ImageSource(snapshot.data, diskCache.fileSystem),
                mimeType = "image/png",
                dataSource = DataSource.DISK
            )
        }

        val info = packageManager.getPackageInfo(request.packageName, 0)
            ?: throw Exception("Failed to parse APK")

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

    class Factory(private val packageManager: PackageManager) : Fetcher.Factory<AppIconRequest> {
        override fun create(data: AppIconRequest, options: Options, imageLoader: ImageLoader): Fetcher {
            return AppIconFetcher(data, packageManager, imageLoader.diskCache)
        }
    }
}

data class AppIconRequest(
    val packageName: String,
    val lastUpdate: Long
)