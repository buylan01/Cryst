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

package com.buylan.cryst.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.model.AppViewModel
import com.buylan.cryst.model.DarkMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(appViewModel: AppViewModel, onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "返回")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.displayCutout
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_group_appearance),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            item {
                var showDarkModeMenu by remember { mutableStateOf(false) }
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_dark_mode_title)) },
                    modifier = Modifier.clickable(onClick = { showDarkModeMenu = true }),
                    leadingContent = { Icon(painter = painterResource(R.drawable.ic_dark_mode), contentDescription = null) },
                    supportingContent = { Text(stringResource(R.string.settings_dark_mode_description)) },
                    trailingContent = {
                        Text(text = stringResource(appViewModel.darkMode.label))
                        DropdownMenu(
                            expanded = showDarkModeMenu,
                            onDismissRequest = { showDarkModeMenu = false }
                        ) {
                            DarkMode.entries.forEach {
                                DropdownMenuItem(
                                    text = { Text(stringResource(it.label)) },
                                    onClick = {
                                        appViewModel.setDarkTheme(it)
                                        showDarkModeMenu = false
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}