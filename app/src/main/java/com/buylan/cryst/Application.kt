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

package com.buylan.cryst

import android.app.Application
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.CachePolicy
import coil3.util.DebugLogger
import com.buylan.cryst.coli.ApkIconFetcher
import com.buylan.cryst.coli.ApkIconKeyer
import com.buylan.cryst.model.AppViewModel
import java.io.File

class Application : Application() {
    lateinit var appViewModel: AppViewModel
        private set

    lateinit var apkImageLoader: ImageLoader
        private set

    override fun onCreate() {
        super.onCreate()
        appViewModel = AppViewModel(this)
        clearArchiveCache()

        apkImageLoader = ImageLoader.Builder(applicationContext)
            .components {
                add(ApkIconFetcher.Factory(packageManager))
                add(ApkIconKeyer())
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(applicationContext.cacheDir.resolve("coil_apk_icon"))
                    .maxSizePercent(0.03)
                    .build()
            }
            .logger(DebugLogger())
            .build()
    }

    private fun clearArchiveCache() {
        try {
            val dir = File(cacheDir, "archive_cache")
            if (dir.exists()) dir.deleteRecursively()
        } catch (_: Exception) {}
    }

}