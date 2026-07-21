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

package com.buylan.cryst.ui.screen.libraries

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.util.withJson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(onBackPressed: () -> Unit) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    val navigationBarHeight = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(stringResource(R.string.about_libraries)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "返回")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.displayCutout
    ) { innerPadding ->

        val libs = Libs.Builder()
            .withJson(context, R.raw.aboutlibraries)
            .build()

        var showLicenseDialog by remember { mutableStateOf(false) }
        var license by remember { mutableStateOf<Set<License>?>(null) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = navigationBarHeight)
        ) {
            items(libs.libraries) { item ->
                Row(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                license = item.licenses
                                showLicenseDialog = true
                            }
                        )
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            item.developers.joinToString(", ") { it.name.toString() },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (showLicenseDialog) {
            license?.forEach {
                AlertDialog(
                    onDismissRequest = { showLicenseDialog = false },
                    title = {
                        Text(it.name)
                    },
                    text = {
                        it.licenseContent?.let { content ->
                            Text(content, modifier = Modifier.verticalScroll(rememberScrollState()))
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showLicenseDialog = false }
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    }
                )
            }
        }
    }
}