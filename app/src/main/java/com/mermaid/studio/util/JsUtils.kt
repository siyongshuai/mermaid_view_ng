package com.mermaid.studio.util

/**
 * Utility functions for JavaScript interop
 */
object JsUtils {
    
    /**
     * Encodes a string for safe use in JavaScript
     */
    fun encodeForJs(str: String): String {
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\u2028", "\\u2028")
            .replace("\u2029", "\\u2029")
    }
    
    /**
     * Wraps code in a JavaScript string literal
     */
    fun toJsString(str: String): String {
        return "\"${encodeForJs(str)}\""
    }
}

