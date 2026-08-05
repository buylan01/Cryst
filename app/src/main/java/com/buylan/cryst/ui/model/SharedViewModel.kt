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

package com.buylan.cryst.ui.model

import android.app.Application
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buylan.cryst.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Application.dataStore by preferencesDataStore(name = "settings")

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    var darkMode: DarkMode by mutableStateOf(DarkMode.System)
        private set

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result.asStateFlow()

    fun setResult(value: String) {
        _result.value = value
    }

    fun consumeResult() {
        _result.value = null
    }

    init {
        viewModelScope.launch {
            darkMode = getSavedDarkMode()
        }
    }

    fun setDarkTheme(theme: DarkMode) {
        darkMode = theme
        viewModelScope.launch {
            saveDarkMode(theme)
        }
    }

    fun isDarkMode(): Boolean {
        return when (darkMode) {
            DarkMode.System -> isSystemInDark()
            DarkMode.Light  -> false
            DarkMode.Dark   -> true
        }
    }

    fun toggleDarkMode() {
        when (darkMode) {
            DarkMode.System -> {
                if (isSystemInDark()) {
                    setDarkTheme(DarkMode.Light)
                } else {
                    setDarkTheme(DarkMode.Dark)
                }
            }

            DarkMode.Light -> {
                setDarkTheme(DarkMode.Dark)
            }

            DarkMode.Dark -> {
                setDarkTheme(DarkMode.Light)
            }
        }
    }

    private suspend fun saveDarkMode(theme: DarkMode) {
        getApplication<Application>().dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = theme.name
        }
    }

    private suspend fun getSavedDarkMode(): DarkMode {
        val pref = getApplication<Application>().dataStore.data
            .map { it[DARK_MODE_KEY] ?: DarkMode.System.name }
            .first()
        return try {
            DarkMode.valueOf(pref)
        } catch (_: IllegalArgumentException) {
            DarkMode.System
        }
    }

    private fun isSystemInDark(): Boolean {
        val uiMode = getApplication<Application>().resources.configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    companion object {
        private val DARK_MODE_KEY = stringPreferencesKey("dark_mode")
    }
}

enum class DarkMode(
    val label: Int,
    val icon: Int
) {
    System(R.string.follow_system, R.drawable.ic_night_sight_auto),
    Light(R.string.light, R.drawable.ic_light_mode),
    Dark(R.string.dark, R.drawable.ic_dark_mode)
}