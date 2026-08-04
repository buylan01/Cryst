package com.buylan.cryst.ui.screen.home

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.fastForEachIndexed
import com.buylan.cryst.R
import com.buylan.cryst.activity.SettingsActivity
import com.buylan.cryst.ui.screen.home.model.DialogsEvent
import com.buylan.cryst.ui.screen.home.model.HomeViewModel
import com.buylan.cryst.ui.screen.home.model.PanelStates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    currentPanel: PanelStates,
    viewModel: HomeViewModel,
    scope: CoroutineScope,
    drawerState: DrawerState,
    context: Context
) {

    val menuItemLabels = remember {
        listOf(
            R.string.refresh,
            R.string.search,
            R.string.sort,
            R.string.settings,
            R.string.exit
        )
    }
    val menuItemIcons = remember {
        listOf(
            R.drawable.ic_refresh,
            R.drawable.ic_search,
            R.drawable.ic_sort,
            R.drawable.ic_settings,
            R.drawable.ic_exit_to_app
        )
    }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = currentPanel.path.pathDisplay,
                    maxLines = 1,
                    overflow = TextOverflow.StartEllipsis,
                    softWrap = false,
                    modifier = Modifier.clickable(
                        onClick = {
                            viewModel.dialogsViewModel.show(DialogsEvent.PathDialog)
                        }
                    )
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    scope.launch {
                        if (drawerState.isClosed) {
                            drawerState.open()
                        } else {
                            drawerState.close()
                        }
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = null
                )
            }
        },
        actions = {
            Box {
                var expanded by remember { mutableStateOf(false) }

                IconButton(
                    onClick = {
                        expanded = true
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = null
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    menuItemLabels.fastForEachIndexed { itemIndex, itemLabel ->
                        DropdownMenuItem(
                            text = { Text(stringResource(itemLabel)) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(menuItemIcons[itemIndex]),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                expanded = false
                                when (itemIndex) {
                                    0 -> {
                                        viewModel.refreshPanel()
                                    }

                                    1 -> {
                                        viewModel.dialogsViewModel.show(DialogsEvent.SearchDialog(currentPanel.path))
                                    }

                                    2 -> {
                                        viewModel.dialogsViewModel.show(DialogsEvent.SortDialog)
                                    }

                                    3 -> {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                SettingsActivity::class.java
                                            )
                                        )
                                    }

                                    4 -> {
                                        (context as ComponentActivity).finishAffinity()
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    )
}