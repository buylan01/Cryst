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