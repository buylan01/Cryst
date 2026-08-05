package com.buylan.cryst.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R

@Composable
fun AppIconPlaceholder(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(64.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            modifier = Modifier.padding(8.dp),
            painter = painterResource(R.drawable.ic_android),
            contentDescription = null
        )
    }
}

@Preview(showBackground = false, showSystemUi = false)
@Composable
fun AppIconPlaceholderPreview() {
    AppIconPlaceholder()
}