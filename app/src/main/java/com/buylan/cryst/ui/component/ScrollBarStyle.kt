package com.buylan.cryst.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.github.oikvpqya.compose.fastscroller.ScrollbarStyle
import io.github.oikvpqya.compose.fastscroller.ThumbStyle
import io.github.oikvpqya.compose.fastscroller.TrackStyle

@Composable
fun materialScrollbarStyle() = ScrollbarStyle(
    minimalHeight = 48.dp,
    thickness = 10.dp,
    hoverDurationMillis = 300,
    thumbStyle = ThumbStyle(
        shape = RoundedCornerShape(4.dp),
        unhoverColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        hoverColor = MaterialTheme.colorScheme.outline,
    ),
    trackStyle = TrackStyle(
        shape = RectangleShape,
        unhoverColor = Color.Transparent,
        hoverColor = Color.Transparent,
    )
)