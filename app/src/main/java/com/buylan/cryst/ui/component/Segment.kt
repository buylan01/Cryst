package com.buylan.cryst.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Segment(
    modifier: Modifier,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    onClick: () -> Unit = { }
) {
    Row(modifier = modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        title()
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            text()
        }
    }
}
