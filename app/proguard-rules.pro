# Mermaid Studio ProGuard Rules

# Keep WebView JavaScript interfaces
-keepclassmembers class com.mermaid.studio.webview.** {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Room entities
-keep class com.mermaid.studio.data.local.entity.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
