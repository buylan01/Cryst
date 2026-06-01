package com.buylan.cryst.ui.screen.appmanager

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import com.buylan.cryst.R
import com.buylan.cryst.util.ExtractPath
import com.buylan.cryst.util.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationManager(){
    val context = LocalContext.current
    var selectedDestination by rememberSaveable { mutableStateOf(Destination.USER) }
    var userApps by remember { mutableStateOf(emptyList<PackageInfo>()) }
    var systemApps by remember { mutableStateOf(emptyList<PackageInfo>()) }
    var checkedApp by remember { mutableStateOf<PackageInfo?>(null) }
    var showAppDetailDialog by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val searchField = rememberTextFieldState()
    val pm = context.packageManager
    val scope = rememberCoroutineScope()

    val filteredUserApps = remember(userApps,searchField.text) {
        if (!searchActive) {
            userApps
        } else {
            val text = searchField.text
            userApps.filter { app ->
                app.applicationInfo!!.loadLabel(context.packageManager)
                    .toString()
                    .contains(text, ignoreCase = true) || app.packageName.contains(text)
            }
        }
    }

    val filteredSystemApps = remember(systemApps,searchField.text) {
        val text = searchField.text
        if (!searchActive) {
            systemApps
        } else {
            systemApps.filter { app ->
                app.applicationInfo!!.loadLabel(context.packageManager)
                    .toString()
                    .contains(text, ignoreCase = true)|| app.packageName.contains(text)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!searchActive) {
                        Text(stringResource(R.string.apps))
                    } else {
                        val focusRequester = remember { FocusRequester() }
                        val colors = TextFieldDefaults.colors()
                        BasicTextField(
                            state = searchField,
                            lineLimits = TextFieldLineLimits.SingleLine,
                            textStyle = MaterialTheme.typography.titleMedium.merge(TextStyle(color = colors.focusedTextColor)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            cursorBrush = SolidColor(colors.cursorColor)
                        )

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { (context as ComponentActivity).finish() }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                },
                actions = {

                    var showMenu by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = {
                            val text = searchField.text
                            if (text.isEmpty()) {
                                searchActive = !searchActive
                            } else {
                                searchField.clearText()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(if (searchActive) R.drawable.ic_close else R.drawable.ic_search),
                            contentDescription = null
                        )
                    }

                    IconButton(
                        onClick = {
                            showMenu = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort)) },
                            onClick = { Toast.makeText(context, "这里没做其实是作者的一个小巧思喵, 有意见去gayhub反馈喵",
                                Toast.LENGTH_LONG).show() }
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0,0,0,0)
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            PrimaryTabRow(
                selectedTabIndex = selectedDestination.ordinal
            ) {
                Destination.entries.forEachIndexed { index, destination ->
                    Tab(
                        selected = selectedDestination == Destination.entries[index],
                        onClick = {
                            Destination.entries
                            selectedDestination = Destination.entries[index]
                        },
                        text = {
                            Text(
                                text = stringResource(destination.label),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            LaunchedEffect(Unit) {
                val allApps = withContext(Dispatchers.IO) {
                    pm.getInstalledPackages(PackageManager.GET_META_DATA)
                }

                val sortedApps = allApps.sortedByDescending { it.lastUpdateTime }

                userApps = sortedApps.filter {
                    it.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }
                systemApps = sortedApps.filter {
                    it.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM != 0
                }
                isLoading = false
            }

            fun handleAppClick(
                app: PackageInfo
            ) {
                checkedApp = app
                showAppDetailDialog = true
            }

            val animation = (fadeIn(animationSpec = tween(220,0)) + scaleIn(
                initialScale = 0.99f, animationSpec = tween(220,0)
            )).togetherWith(fadeOut(animationSpec = tween(0,0)))

            if (!isLoading) {
                AnimatedContent(
                    targetState = selectedDestination,
                    transitionSpec = { animation }
                ) { it ->
                    when (it) {
                        Destination.USER -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    filteredUserApps,
                                    key = { app -> app.packageName }
                                ) { app ->
                                    AppItem(app, pm) { handleAppClick(it) }
                                }
                                item {
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }

                        Destination.SYSTEM -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    filteredSystemApps,
                                    key = { app -> app.packageName }
                                ) { app ->
                                    AppItem(app, pm) { handleAppClick(it) }
                                }
                                item {
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { CircularProgressIndicator() }
            }

            if (showAppDetailDialog) {
                val app = checkedApp!!
                val appName = pm.getApplicationLabel(app.applicationInfo!!).toString()
                AlertDialog(
                    onDismissRequest = { showAppDetailDialog = false },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Row(
                                modifier = Modifier.padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val appIcon = remember(app.packageName) {
                                    try {
                                        pm.getApplicationIcon(app.packageName)
                                    } catch (_: Exception) {
                                        null
                                    }
                                }

                                appIcon?.let { icon ->
                                    AsyncImage(
                                        model = icon,
                                        contentDescription = "App icon",
                                        modifier = Modifier.size(48.dp),
                                        contentScale = ContentScale.Fit,
                                        placeholder = ColorPainter(Color.LightGray)
                                    )
                                } ?: Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.LightGray)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = appName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = app.versionName.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.fillMaxWidth())

                            Spacer(Modifier.height(8.dp))

                            Column {
                                InfoItem("包名", app.packageName)
                                InfoItem("版本号", app.longVersionCode.toString())
                                InfoItem("大小", formatFileSize(File(app.applicationInfo!!.sourceDir).length()))
                                InfoItem("数据目录", app.applicationInfo!!.dataDir)
                                InfoItem("安装目录", app.applicationInfo!!.sourceDir)
                                InfoItem("UID", app.applicationInfo!!.uid.toString())
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    val target = File(ExtractPath)
                                    if (!target.exists()) {
                                        target.mkdir()
                                    }
                                    val copy = try {
                                        File(app.applicationInfo!!.sourceDir).copyTo(File("$ExtractPath/${appName}_${app.versionName}.apk"))
                                        true
                                    } catch (_: IOException) {
                                        false
                                    }
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, if (copy) "提取成功, 文件被保存在$ExtractPath" else "提取失败",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(R.string.extract))
                        }
                    },
                    dismissButton = {
                        Row {
                            IconButton(
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_DELETE)
                                        .apply {
                                            data = Uri.fromParts("package", app.packageName, null)
                                        }
                                    )
                                }
                            ) {
                                Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = null)
                            }
                            IconButton(
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .apply {
                                            data = Uri.fromParts("package", app.packageName, null)
                                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    )
                                }
                            ) {
                                Icon(painter = painterResource(R.drawable.ic_info), contentDescription = null)
                            }
                            IconButton(
                                onClick = {
                                    try {
                                        context.startActivity(pm.getLaunchIntentForPackage(app.packageName))
                                    } catch (_: NullPointerException) {
                                        Toast.makeText(context, "应用没有主活动", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(painter = painterResource(R.drawable.ic_open_in_new), contentDescription = null)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InfoItem(title: String, summary: String) {
    Row(modifier = Modifier.width(230.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title)
        Text(
            text = summary,
            softWrap = false,
            modifier = Modifier
                .widthIn(max = 160.dp)
                .combinedClickable(
                    onClick = {

                    }),
            overflow = TextOverflow.MiddleEllipsis,
            textAlign = TextAlign.End
        )
    }
    Spacer(Modifier.height(4.dp))
}
@Composable
fun AppItem(
    app: PackageInfo,
    packageManager: PackageManager,
    onClick: (PackageInfo) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(app) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val px = LocalDensity.current.run { 48.dp.toPx().toInt() }
            val appIcon = remember(app.packageName) {
                try {
                    val drawable = packageManager.getApplicationIcon(app.packageName)
                    drawable.toBitmap(
                        width = px,
                        height = px
                    ).asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            }

            appIcon?.let { icon ->
                Image(
                    bitmap = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = packageManager.getApplicationLabel(app.applicationInfo!!).toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.padding(vertical = 2.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

enum class Destination(
    val label: Int
) {
    USER(R.string.user),
    SYSTEM(R.string.system),
}