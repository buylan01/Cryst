package com.buylan.cryst.ui.screen.apps.model

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.buylan.cryst.ui.MainActivity
import com.buylan.cryst.ui.screen.apps.AppsDestination
import com.buylan.cryst.ui.screen.apps.AppsSortType
import com.buylan.cryst.util.ExtractPath
import com.buylan.cryst.util.toApkInfo
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

    fun showAppDialog(data: ApkInfo) {
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
        source: ApkInfo
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val target = File(ExtractPath)
            if (!target.exists()) {
                target.mkdir()
            }
            val outPath =
                "${ExtractPath}/${source.label}_${source.versionName}.apk"


            val copy = try {
                File(source.installedSource!!).copyTo(File(outPath))
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
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val allApps = withContext(Dispatchers.IO) {
                    packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
                }

                val userApps = allApps
                    .filter { it.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                    .map { it.toApkInfo(packageManager) }

                val systemApps = allApps
                    .filter { it.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM != 0 }
                    .map { it.toApkInfo(packageManager) }

                _uiState.update { current ->
                    current.copy(
                        userApps = userApps,
                        systemApps = systemApps,
                        isLoading = false
                    )
                }
            }
        }
    }
}