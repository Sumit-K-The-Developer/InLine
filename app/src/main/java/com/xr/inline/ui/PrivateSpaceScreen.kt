package com.xr.inline.ui

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val PRIVATE_SPACE_PREFS = "private_space"
private const val WEB_APPS_KEY = "web_apps"
private val privateSpaceJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class WebAppShortcut(
    val name: String,
    val url: String
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PrivateSpaceScreen(
    onExit: () -> Unit,
    onOpenLogin: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val webApps = remember(context) {
        mutableStateListOf<WebAppShortcut>().apply {
            addAll(loadWebApps(context))
        }
    }
    var selectedApp by remember { mutableStateOf<WebAppShortcut?>(null) }
    var isAddDialogVisible by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedApp == null) {
        onExit()
    }

    selectedApp?.let { app ->
        PrivateWebAppScreen(
            app = app,
            onBack = { selectedApp = null },
            onOpenLogin = onOpenLogin
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Private space") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to news"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isAddDialogVisible = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add web app"
                )
            }
        }
    ) { innerPadding ->
        if (webApps.isEmpty()) {
            EmptyPrivateSpace(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onAddWebApp = { isAddDialogVisible = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Web apps stay inside this space.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                items(webApps, key = { it.url }) { app ->
                    WebAppCard(
                        app = app,
                        onOpen = { selectedApp = app },
                        onDelete = {
                            webApps.remove(app)
                            saveWebApps(context, webApps)
                        }
                    )
                }
            }
        }
    }

    if (isAddDialogVisible) {
        AddWebAppDialog(
            onDismiss = { isAddDialogVisible = false },
            onAdd = { name, url ->
                webApps.add(WebAppShortcut(name = name, url = url))
                saveWebApps(context, webApps)
                isAddDialogVisible = false
            }
        )
    }
}

@Composable
private fun EmptyPrivateSpace(
    modifier: Modifier = Modifier,
    onAddWebApp: () -> Unit
) {
    Box(
        modifier = modifier.padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Your private space is empty",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Add a website to open it here instead of leaving the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onAddWebApp) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add web app", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun WebAppCard(
    app: WebAppShortcut,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = app.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove ${app.name}"
                )
            }
        }
    }
}

@Composable
private fun AddWebAppDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, url: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add web app") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        urlError = null
                    },
                    label = { Text("Website URL") },
                    placeholder = { Text("https://example.com") },
                    singleLine = true,
                    isError = urlError != null,
                    supportingText = urlError?.let { error ->
                        { Text(error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val normalizedUrl = normalizeUrl(url)
                    if (normalizedUrl == null) {
                        urlError = "Enter a valid website URL"
                    } else if (name.trim().isBlank()) {
                        urlError = "Enter a name"
                    } else {
                        onAdd(name.trim(), normalizedUrl)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PrivateWebAppScreen(
    app: WebAppShortcut,
    onBack: () -> Unit,
    onOpenLogin: (String) -> Unit
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    val navigateBack = {
        val currentWebView = webView
        if (currentWebView != null && currentWebView.canGoBack()) {
            currentWebView.goBack()
        } else {
            onBack()
        }
    }

    BackHandler {
        navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to private space"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        PrivateWebView(
            url = app.url,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            onWebViewCreated = { webView = it },
            onOpenLogin = onOpenLogin
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PrivateWebView(
    url: String,
    modifier: Modifier = Modifier,
    onWebViewCreated: (WebView) -> Unit,
    onOpenLogin: (String) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.userAgentString = settings.userAgentString
                    .replace("; wv", "")
                    .replace("Version/4.0 ", "")
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: android.webkit.WebResourceRequest
                    ): Boolean {
                        val requestedUrl = request.url
                        return if (isInstagramLoginUrl(requestedUrl)) {
                            onOpenLogin(requestedUrl.toString())
                            true
                        } else {
                            false
                        }
                    }
                }
                onWebViewCreated(this)
                if (isInstagramLoginUrl(Uri.parse(url))) {
                    onOpenLogin(url)
                } else {
                    loadUrl(url)
                }
            }
        },
        onRelease = { webView ->
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.destroy()
        }
    )
}

private fun isInstagramLoginUrl(uri: Uri): Boolean {
    val host = uri.host?.lowercase() ?: return false
    val path = uri.path.orEmpty()
    val isInstagramHost = host == "instagram.com" || host.endsWith(".instagram.com")
    return isInstagramHost && (
        path.startsWith("/accounts/login") ||
            path == "/login" ||
            path.startsWith("/login/")
        )
}

private fun normalizeUrl(value: String): String? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return null

    val candidate = if (
        trimmed.startsWith("https://", ignoreCase = true) ||
        trimmed.startsWith("http://", ignoreCase = true)
    ) {
        trimmed
    } else {
        "https://$trimmed"
    }

    val parsed = Uri.parse(candidate)
    return candidate.takeIf {
        parsed.host != null && parsed.scheme in setOf("http", "https")
    }
}

private fun loadWebApps(context: Context): List<WebAppShortcut> {
    val stored = context
        .getSharedPreferences(PRIVATE_SPACE_PREFS, Context.MODE_PRIVATE)
        .getString(WEB_APPS_KEY, null)
        ?: return emptyList()

    return runCatching {
        privateSpaceJson.decodeFromString<List<WebAppShortcut>>(stored)
    }.getOrDefault(emptyList())
}

private fun saveWebApps(context: Context, webApps: List<WebAppShortcut>) {
    val encoded = privateSpaceJson.encodeToString(webApps)
    context
        .getSharedPreferences(PRIVATE_SPACE_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(WEB_APPS_KEY, encoded)
        .apply()
}
