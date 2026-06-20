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

package com.buylan.cryst.ui.screen.apps

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.buylan.cryst.activity.MainActivity
import com.buylan.cryst.util.ExtractPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class AppsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppsStates())
    val uiState: StateFlow<AppsStates> = _uiState.asStateFlow()

    private val packageManager = application.packageManager

    fun setSearchActive(active: Boolean) {
        _uiState.update {
            it.copy(
                searchActive = active
            )
        }
    }

    fun showAppDialog(data: PackageInfo) {
        _uiState.value = _uiState.value.copy(appDialog = data)
    }

    fun hideAppDialog() {
        _uiState.value = _uiState.value.copy(appDialog = null)
    }

    fun showLocateDialog(data: String) {
        _uiState.value = _uiState.value.copy(locateDialog = data)
    }

    fun hideLocateDialog() {
        _uiState.value = _uiState.value.copy(locateDialog = null)
    }

    fun showMenu() {
        _uiState.value = _uiState.value.copy(showMenu = true)
    }

    fun hideMenu() {
        _uiState.value = _uiState.value.copy(showMenu = false)
    }

    fun showSortDialog() {
        _uiState.value = _uiState.value.copy(showSortDialog = true)
    }

    fun hideSortDialog() {
        _uiState.value = _uiState.value.copy(showSortDialog = false)
    }

    fun setDestination(destination: AppsDestination) {
        _uiState.update {
            it.copy(
                selectedDestination = destination
            )
        }
    }

    fun setSortType(sortType: AppsSortType) {
        _uiState.update {
            it.copy(
                sortType = sortType
            )
        }
    }

    fun onExtract(
        source: PackageInfo
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val target = File(ExtractPath)
            if (!target.exists()) {
                target.mkdir()
            }
            val outPath =
                "$ExtractPath/${source.applicationInfo!!.loadLabel(packageManager)}_${source.versionName}.apk"


            val copy = try {
                File(source.applicationInfo!!.sourceDir).copyTo(File(outPath))
                true
            } catch (_: IOException) {
                false
            }
            withContext(Dispatchers.Main) {
                if (copy) {
                    showLocateDialog(outPath)
                    hideAppDialog()
                } else {
                    Toast.makeText(
                        application,
                        "提取失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun onLocate(path: String) {
        application.startActivity(
            Intent(
                application,
                MainActivity::class.java
            ).apply {
                putExtra("path", path)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    init {
        viewModelScope.launch {
            val allApps = withContext(Dispatchers.IO) {
                packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            }

            _uiState.update { current ->
                current.copy(
                    userApps = allApps.filter { it.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM == 0 },
                    systemApps = allApps.filter { it.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM != 0 },
                    isLoading = false
                )
            }
        }
    }
}