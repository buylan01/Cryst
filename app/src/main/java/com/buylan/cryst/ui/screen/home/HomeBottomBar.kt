package com.buylan.cryst.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.util.fastForEach
import com.buylan.cryst.R
import com.buylan.cryst.ui.screen.home.model.DialogsEvent
import com.buylan.cryst.ui.screen.home.model.HomeViewModel
import com.buylan.cryst.ui.screen.home.model.PanelStates

@Composable
fun HomeBottomBar(
    viewModel: HomeViewModel,
    currentPanel: PanelStates
) {

    val bottomActions =
        listOf(
            BottomAction(
                icon = R.drawable.ic_keyboard_arrow_left,
                enabled = viewModel.currentPanelViewModel.canUnNavigate,
                onClick = { viewModel.currentPanelViewModel.unNavigate() }
            ),
            BottomAction(
                icon = R.drawable.ic_keyboard_arrow_right,
                enabled = viewModel.currentPanelViewModel.canReNavigate,
                onClick = { viewModel.currentPanelViewModel.reNavigate() }
            ),
            BottomAction(
                icon = R.drawable.ic_add,
                onClick = { viewModel.dialogsViewModel.show(DialogsEvent.CreateDialog(currentPanel.path)) }
            ),
            BottomAction(
                icon = R.drawable.ic_swap_horiz,
                onClick = { viewModel.anotherPanelViewModel.setPath(currentPanel.path) }
            ),
            BottomAction(
                icon = R.drawable.ic_arrow_upward,
                onClick = { viewModel.onNavigateBack() }
            ),
        )

    BottomAppBar(
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bottomActions.fastForEach { action ->
                    IconButton(
                        onClick = action.onClick,
                        enabled = action.enabled
                    ) {
                        Icon(
                            painter = painterResource(action.icon),
                            contentDescription = action.contentDescription,
                        )
                    }
                }
            }
        }
    )
}

data class BottomAction(
    val icon: Int,
    val contentDescription: String? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)