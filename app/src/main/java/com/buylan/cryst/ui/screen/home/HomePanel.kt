package com.buylan.cryst.ui.screen.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.buylan.cryst.ui.component.AutoScrollBar
import com.buylan.cryst.ui.screen.home.model.DialogsEvent
import com.buylan.cryst.ui.screen.home.model.HomeUiState
import com.buylan.cryst.ui.screen.home.model.HomeViewModel
import com.buylan.cryst.ui.screen.home.model.PanelStates
import com.buylan.cryst.ui.screen.home.model.PanelViewModel
import com.buylan.cryst.util.PanelPosition
import com.buylan.cryst.util.getFileType
import com.buylan.cryst.vfs.VirtualFile
import kotlinx.coroutines.flow.first

@Composable
fun HomePanel(
    modifier: Modifier = Modifier,
    panelState: PanelStates,
    panelViewModel: PanelViewModel,
    lazyState: LazyListState,
    panelPosition: PanelPosition,
    viewModel: HomeViewModel,
    uiState: HomeUiState,
    onClickFile: (VirtualFile) -> Unit
) {

    LaunchedEffect(Unit) {
        panelViewModel.scrollToIndex.collect { index ->
            snapshotFlow { lazyState.layoutInfo.totalItemsCount }.first { it >= index }
            lazyState.scrollToItem(index)
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (panelPosition == uiState.panelPosition) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLowest,
        animationSpec = tween(150)
    )
    Box(
        modifier = modifier
            .fillMaxHeight()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(backgroundColor)
                }
                .pointerInput(panelPosition) {
                    awaitEachGesture {
                        awaitFirstDown(
                            requireUnconsumed = false,
                            pass = PointerEventPass.Initial
                        )
                        viewModel.setPanel(panelPosition)
                    }
                },
            state = lazyState
        ) {
            item {
                UpwardItem { panelViewModel.navigateBack() }
            }
            items(panelState.files, key = { it.hashCode() }) { file ->
                FileItem(
                    file = file,
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(durationMillis = 210, delayMillis = 10),
                        fadeOutSpec = null
                    ),
                    type = getFileType(file),
                    highLight = file.name in panelState.highLightFiles,
                    selected = file.path in panelState.selectedFiles,
                    onClick = {
                        if (panelViewModel.selectionMode) {
                            panelViewModel.toggleSelection(file)
                        } else {
                            onClickFile(file)
                        }
                    },
                    onLongClick = {
                        viewModel.dialogsViewModel.show(
                            DialogsEvent.ToolsDialog(
                                panelViewModel.getSelectedFiles(file)
                            )
                        )
                    },
                    onSwipe = {
                        panelViewModel.swipeSelect(file)
                    }
                )
            }
        }
        AutoScrollBar(
            lazyState = lazyState,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}