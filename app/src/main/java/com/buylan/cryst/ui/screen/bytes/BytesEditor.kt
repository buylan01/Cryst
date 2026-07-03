package com.buylan.cryst.ui.screen.bytes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.buylan.cryst.R
import com.buylan.cryst.ui.component.AutoScrollBar
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.ceil

private const val BYTES_PER_ROW = 8
private const val MAX_CACHED_ROWS = 500

data class HexRow(
    val offset: Long,
    val bytes: ByteArray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BytesEditor(
    file: File,
    onBack: () -> Unit
) {
    val rowCount = remember(file) {
        ceil(file.length() / BYTES_PER_ROW.toDouble()).toInt()
    }

    val raf = remember(file) {
        RandomAccessFile(file, "r")
    }
    DisposableEffect(file) {
        onDispose { raf.close() }
    }

    val cache = remember {
        object : LinkedHashMap<Int, HexRow>(MAX_CACHED_ROWS, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, HexRow>?): Boolean {
                return size > MAX_CACHED_ROWS
            }
        }
    }

    val lazyListState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(file.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = null
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.displayCutout
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState
            ) {
                items(count = rowCount) { index ->
                    val row = remember(index) {
                        cache.getOrPut(index) { readRow(raf, index) }
                    }
                    HexRowItem(row)
                }
            }
            AutoScrollBar(
                lazyState = lazyListState,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

private fun readRow(raf: RandomAccessFile, index: Int): HexRow {
    val offset = index.toLong() * BYTES_PER_ROW
    raf.seek(offset)

    val buffer = ByteArray(BYTES_PER_ROW)
    val bytesRead = raf.read(buffer)

    return HexRow(
        offset = offset,
        bytes = if (bytesRead == BYTES_PER_ROW) buffer else buffer.copyOf(bytesRead)
    )
}

@Composable
private fun HexRowItem(row: HexRow) {
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Text(
            text = "%04X".format(row.offset),
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall
        )

        VerticalDivider(
            modifier = Modifier
                .height(18.dp)
                .padding(horizontal = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(BYTES_PER_ROW) { index ->
                val text = if (index < row.bytes.size)
                    "%02X".format(row.bytes[index].toInt() and 0xFF)
                else
                    "  "

                Text(
                    text = text,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(22.dp)
                )

                if (index == 3) {
                    VerticalDivider(
                        modifier = Modifier
                            .height(18.dp)
                            .padding(horizontal = 2.dp)
                    )
                }
            }
        }

        VerticalDivider(
            modifier = Modifier
                .height(18.dp)
                .padding(end = 8.dp)
        )

        Text(
            text = row.bytes.toString(Charsets.US_ASCII),
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}