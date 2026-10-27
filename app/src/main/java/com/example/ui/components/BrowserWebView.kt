package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.BrowserSettings
import com.example.ui.BrowserViewModel

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    viewModel: BrowserViewModel,
    targetUrl: String,
    isDesktopSite: Boolean,
    browserSettings: BrowserSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var customVideoView by remember { mutableStateOf<View?>(null) }
    var customVideoCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    // Synchronize actions with ViewModel
    LaunchedEffect(webViewRef) {
        val webView = webViewRef ?: return@LaunchedEffect
        viewModel.onWebGoBack = {
            if (webView.canGoBack()) webView.goBack()
        }
        viewModel.onWebGoForward = {
            if (webView.canGoForward()) webView.goForward()
        }
        viewModel.onWebReload = {
            hasError = false
            webView.reload()
        }
        viewModel.onWebStop = {
            webView.stopLoading()
        }
        viewModel.onWebLoadUrl = { url ->
            hasError = false
            webView.loadUrl(url)
        }
        viewModel.onWebFindText = { query ->
            if (query.isNotBlank()) {
                webView.findAllAsync(query)
            } else {
                webView.clearMatches()
                viewModel.updateFindMatches(0, 0)
            }
        }
        viewModel.onWebFindNext = { forward ->
            webView.findNext(forward)
        }
        viewModel.onWebClearMatches = {
            webView.clearMatches()
            viewModel.updateFindMatches(0, 0)
        }
        viewModel.onWebExtractReader = {
            // Extract readability text
            webView.evaluateJavascript(
                "(function() { return (document.querySelector('article') || document.body).innerText; })();"
            ) { result ->
                if (!result.isNullOrBlank() && result != "null") {
                    val unescaped = result.trim('"')
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                    viewModel.setReaderContent(unescaped)
                }
            }
        }
    }

    // React to Desktop Site toggle or Settings change
    LaunchedEffect(isDesktopSite, browserSettings) {
        val webView = webViewRef ?: return@LaunchedEffect
        val defaultUa = WebSettings.getDefaultUserAgent(context)
        webView.settings.userAgentString = if (isDesktopSite) DESKTOP_USER_AGENT else defaultUa
        webView.settings.javaScriptEnabled = browserSettings.javascriptEnabled
        webView.settings.textZoom = browserSettings.textScaling
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasError) {
            ErrorFallbackView(
                error = errorMessage,
                onRetry = {
                    hasError = false
                    webViewRef?.loadUrl(targetUrl)
                }
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        this.settings.apply {
                            javaScriptEnabled = browserSettings.javascriptEnabled
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            textZoom = browserSettings.textScaling
                            userAgentString = if (isDesktopSite) DESKTOP_USER_AGENT else WebSettings.getDefaultUserAgent(ctx)
                        }

                        setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                            viewModel.updateFindMatches(activeMatchOrdinal, numberOfMatches)
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                viewModel.onProgressChanged(newProgress)
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                super.onReceivedTitle(view, title)
                                if (!title.isNullOrBlank()) {
                                    viewModel.onPageFinished(
                                        url = view?.url ?: targetUrl,
                                        title = title,
                                        canBack = view?.canGoBack() ?: false,
                                        canForward = view?.canGoForward() ?: false
                                    )
                                }
                            }

                            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                customVideoView = view
                                customVideoCallback = callback
                            }

                            override fun onHideCustomView() {
                                customVideoCallback?.onCustomViewHidden()
                                customVideoView = null
                                customVideoCallback = null
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val nextUrl = request?.url?.toString() ?: return false
                                if (nextUrl.startsWith("http://") || nextUrl.startsWith("https://")) {
                                    return false // Let WebView load it
                                }
                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                hasError = false
                                url?.let { viewModel.onPageStarted(it) }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (url != null) {
                                    viewModel.onPageFinished(
                                        url = url,
                                        title = view?.title,
                                        canBack = view?.canGoBack() ?: false,
                                        canForward = view?.canGoForward() ?: false
                                    )
                                }
                            }

                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    hasError = true
                                    errorMessage = error?.description?.toString() ?: "Page could not be loaded."
                                }
                            }
                        }

                        loadUrl(targetUrl)
                        webViewRef = this
                    }
                },
                update = { webView ->
                    webViewRef = webView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("browser_webview")
            )
        }

        // Fullscreen video layer if invoked by WebChromeClient
        customVideoView?.let { videoView ->
            AndroidView(
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        setBackgroundColor(android.graphics.Color.BLACK)
                        addView(
                            videoView,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.stopLoading()
            webViewRef?.destroy()
            webViewRef = null
        }
    }
}

@Composable
private fun ErrorFallbackView(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Connection Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Webpage Unavailable",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error.ifBlank { "The server could not be reached. Check your internet connection or URL." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.testTag("retry_load_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("Retry")
            }
        }
    }
}
