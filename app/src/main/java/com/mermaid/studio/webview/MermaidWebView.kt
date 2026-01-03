package com.mermaid.studio.webview

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mermaid.studio.util.JsUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Mermaid Diagram Renderer WebView Component
 * 
 * Renders Mermaid diagrams with debounced updates
 */
@Composable
fun MermaidDiagramWebView(
    modifier: Modifier = Modifier,
    mermaidCode: String = "",
    debounceMs: Long = 500L,
    onRenderSuccess: (svg: String) -> Unit = {},
    onRenderError: (error: String) -> Unit = {}
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isReady by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    // Debounced rendering
    LaunchedEffect(mermaidCode, isReady) {
        if (isReady && mermaidCode.isNotBlank()) {
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(debounceMs)
                webView?.evaluateJavascript(
                    "if(typeof renderDiagram==='function'){renderDiagram(${JsUtils.toJsString(mermaidCode)});}",
                    null
                )
            }
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
            debounceJob?.cancel()
            lifecycleOwner.lifecycle.removeObserver(observer)
            webView?.destroy()
            webView = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            createMermaidWebView(
                context = context,
                onReady = { isReady = true },
                onRenderSuccess = onRenderSuccess,
                onRenderError = onRenderError
            ).also { webView = it }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun createMermaidWebView(
    context: Context,
    onReady: () -> Unit,
    onRenderSuccess: (svg: String) -> Unit,
    onRenderError: (error: String) -> Unit
): WebView {
    return WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            textZoom = 100
        }

        isFocusable = true
        isFocusableInTouchMode = true

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onReady()
            }
        }

        addJavascriptInterface(
            MermaidBridge(onRenderSuccess, onRenderError),
            "AndroidBridge"
        )

        loadUrl("file:///android_res/raw/mermaid_renderer.html")
    }
}

/**
 * JavaScript bridge for Mermaid renderer
 */
class MermaidBridge(
    private val onRenderSuccess: (svg: String) -> Unit,
    private val onRenderError: (error: String) -> Unit
) {
    @JavascriptInterface
    fun onRenderSuccess(svg: String) = onRenderSuccess.invoke(svg)

    @JavascriptInterface
    fun onRenderError(error: String) = onRenderError.invoke(error)
}

