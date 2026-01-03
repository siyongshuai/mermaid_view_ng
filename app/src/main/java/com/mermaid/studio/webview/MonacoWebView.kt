package com.mermaid.studio.webview

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mermaid.studio.util.JsUtils

/**
 * Monaco Editor WebView Component
 * 
 * A high-quality code editor based on VS Code's Monaco Editor
 */
@Composable
fun MonacoEditorWebView(
    modifier: Modifier = Modifier,
    initialCode: String = "",
    onCodeChanged: (String) -> Unit = {},
    onEditorReady: () -> Unit = {},
    onWebViewCreated: (WebView) -> Unit = {}
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var lastSentCode by remember { mutableStateOf<String?>(null) }
    var isEditorReady by remember { mutableStateOf(false) }
    var pendingCode by remember { mutableStateOf<String?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Set initial code when editor becomes ready
    LaunchedEffect(isEditorReady) {
        if (isEditorReady && pendingCode != null) {
            webView?.evaluateJavascript(
                "if(typeof setCode==='function'){setCode(${JsUtils.toJsString(pendingCode!!)});}",
                null
            )
            lastSentCode = pendingCode
            pendingCode = null
        }
    }

    // Update editor when initialCode changes from outside
    LaunchedEffect(initialCode) {
        if (isEditorReady) {
            if (initialCode != lastSentCode) {
                lastSentCode = initialCode
                webView?.evaluateJavascript(
                    "if(typeof setCode==='function'){setCode(${JsUtils.toJsString(initialCode)});}",
                    null
                )
            }
        } else {
            // Store pending code to be set when editor is ready
            pendingCode = initialCode
        }
    }

    // Lifecycle management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    webView?.onResume()
                    webView?.resumeTimers()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    webView?.pauseTimers()
                    webView?.onPause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            webView?.destroy()
            webView = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            createMonacoWebView(
                context = context,
                onCodeChanged = { newCode ->
                    lastSentCode = newCode
                    onCodeChanged(newCode)
                },
                onEditorReady = {
                    isEditorReady = true
                    onEditorReady()
                }
            ).also {
                webView = it
                onWebViewCreated(it)
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun createMonacoWebView(
    context: Context,
    onCodeChanged: (String) -> Unit,
    onEditorReady: () -> Unit
): WebView {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    return WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            @Suppress("DEPRECATION")
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(false)
            loadWithOverviewMode = true
            useWideViewPort = true
            textZoom = 100
            defaultTextEncodingName = "utf-8"
        }

        isLongClickable = true
        isFocusable = true
        isFocusableInTouchMode = true

        webViewClient = WebViewClient()
        webChromeClient = WebChromeClient()

        addJavascriptInterface(
            MonacoBridge(
                onCodeChanged = onCodeChanged,
                onEditorReady = onEditorReady,
                onCopyToClipboard = { text ->
                    val clip = ClipData.newPlainText("code", text)
                    clipboardManager.setPrimaryClip(clip)
                }
            ),
            "AndroidBridge"
        )

        loadUrl("file:///android_res/raw/monaco_editor.html")
    }
}

/**
 * JavaScript bridge for Monaco Editor
 */
class MonacoBridge(
    private val onCodeChanged: (String) -> Unit,
    private val onEditorReady: () -> Unit,
    private val onCopyResult: (Boolean) -> Unit = {},
    private val onCopyToClipboard: (String) -> Unit = {}
) {
    @JavascriptInterface
    fun onCodeChanged(code: String) = onCodeChanged.invoke(code)

    @JavascriptInterface
    fun onEditorReady() = onEditorReady.invoke()

    @JavascriptInterface
    fun onCopy(success: Boolean) = onCopyResult.invoke(success)

    @JavascriptInterface
    fun copyToClipboard(text: String) = onCopyToClipboard.invoke(text)
}

