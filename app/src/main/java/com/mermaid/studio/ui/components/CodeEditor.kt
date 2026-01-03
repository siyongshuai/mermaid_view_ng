package com.mermaid.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mermaid.studio.ui.theme.EditorBackground

/**
 * 原生代码编辑器组件
 * 
 * 使用 Jetpack Compose BasicTextField，比 WebView Monaco 更稳定可靠
 */
@Composable
fun CodeEditor(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    
    // 计算行号
    val lines = code.split("\n")
    val lineCount = lines.size
    val maxLineNumberWidth = lineCount.toString().length
    
    Row(
        modifier = modifier
            .background(EditorBackground)
            .padding(top = 8.dp)
    ) {
        // 行号列
        Column(
            modifier = Modifier
                .verticalScroll(verticalScrollState)
                .padding(horizontal = 12.dp)
        ) {
            for (i in 1..lineCount.coerceAtLeast(1)) {
                Text(
                    text = i.toString().padStart(maxLineNumberWidth, ' '),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 22.sp
                    )
                )
            }
        }
        
        // 分隔线
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Color(0xFF3A3A5A))
        )
        
        // 代码编辑区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
                .padding(start = 12.dp, end = 8.dp)
        ) {
            BasicTextField(
                value = code,
                onValueChange = onCodeChange,
                readOnly = readOnly,
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Color(0xFFF8F8FC),
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(Color(0xFF7C3AED)),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    autoCorrectEnabled = false
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 200.dp),
                decorationBox = { innerTextField ->
                    if (code.isEmpty()) {
                        Text(
                            text = "在此输入 Mermaid 代码...",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                lineHeight = 22.sp
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

