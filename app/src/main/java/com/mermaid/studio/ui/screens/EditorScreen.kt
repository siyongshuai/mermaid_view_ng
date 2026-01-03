package com.mermaid.studio.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mermaid.studio.ui.components.CodeEditor
import com.mermaid.studio.ui.theme.*
import com.mermaid.studio.webview.MermaidDiagramWebView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel = hiltViewModel()
) {
    val editorState by viewModel.editorState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    
    var showTemplates by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var currentTab by remember { mutableIntStateOf(0) } // 0: Editor, 1: Preview

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        // Top App Bar
        EditorTopBar(
            isDirty = editorState.isDirty,
            onSave = { showSaveDialog = true },
            onNewDiagram = { viewModel.newDiagram() },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onPaste = { viewModel.pasteFromClipboard() },
            onCopy = { viewModel.copyAllToClipboard() }
        )

        // Main Content
        if (isLandscape) {
            // Landscape: Split view
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Editor Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 4.dp)
                ) {
                    EditorPanel(
                        code = editorState.code,
                        onCodeChanged = viewModel::onCodeChanged
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                // Preview Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 4.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
                ) {
                    PreviewPanel(
                        code = editorState.code,
                        onRenderSuccess = viewModel::onRenderSuccess,
                        onRenderError = viewModel::onRenderError
                    )
                }
            }
        } else {
            // Portrait: Tab view
            Column(modifier = Modifier.weight(1f)) {
                // Tab selector
                TabRow(
                    selectedTabIndex = currentTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        text = { Text("编辑器", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.Code, contentDescription = null) }
                    )
                    Tab(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        text = { Text("预览", fontWeight = FontWeight.Medium) },
                        icon = { Icon(Icons.Default.Preview, contentDescription = null) }
                    )
                }

                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = currentTab == 0,
                        enter = fadeIn() + slideInHorizontally(),
                        exit = fadeOut() + slideOutHorizontally()
                    ) {
                        EditorPanel(
                            code = editorState.code,
                            onCodeChanged = viewModel::onCodeChanged
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = currentTab == 1,
                        enter = fadeIn() + slideInHorizontally { it },
                        exit = fadeOut() + slideOutHorizontally { it }
                    ) {
                        PreviewPanel(
                            code = editorState.code,
                            onRenderSuccess = viewModel::onRenderSuccess,
                            onRenderError = viewModel::onRenderError
                        )
                    }
                }
            }
        }

        // Bottom Action Bar
        BottomActionBar(
            onTemplateClick = { showTemplates = true },
            onPaste = { viewModel.pasteFromClipboard() },
            onCopy = { viewModel.copyAllToClipboard() },
            renderError = editorState.renderError
        )
    }

    // Template Bottom Sheet
    if (showTemplates) {
        TemplateBottomSheet(
            onDismiss = { showTemplates = false },
            onTemplateSelected = { template ->
                viewModel.loadTemplate(template)
                showTemplates = false
            }
        )
    }

    // Save Dialog
    if (showSaveDialog) {
        SaveDiagramDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { title ->
                viewModel.saveDiagram(title)
                showSaveDialog = false
            }
        )
    }
}

@Composable
private fun EditorTopBar(
    isDirty: Boolean,
    onSave: () -> Unit,
    onNewDiagram: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onPaste: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App title with gradient
            Text(
                text = "Mermaid Studio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (isDirty) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Warning)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Clipboard actions - 粘贴按钮（重要！）
            IconButton(onClick = onPaste) {
                Icon(
                    Icons.Default.ContentPaste,
                    contentDescription = "粘贴",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onCopy) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "复制全部",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Edit actions
            IconButton(onClick = onUndo) {
                Icon(
                    Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "撤销",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRedo) {
                Icon(
                    Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "重做",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onNewDiagram) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "新建",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(
                onClick = onSave,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("保存")
            }
        }
    }
}

@Composable
private fun EditorPanel(
    code: String,
    onCodeChanged: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(12.dp),
        color = EditorBackground,
        shadowElevation = 4.dp
    ) {
        CodeEditor(
            code = code,
            onCodeChange = onCodeChanged,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PreviewPanel(
    code: String,
    onRenderSuccess: (String) -> Unit,
    onRenderError: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        MermaidDiagramWebView(
            modifier = Modifier.fillMaxSize(),
            mermaidCode = code,
            onRenderSuccess = onRenderSuccess,
            onRenderError = onRenderError
        )
    }
}

@Composable
private fun BottomActionBar(
    onTemplateClick: () -> Unit,
    onPaste: () -> Unit,
    onCopy: () -> Unit,
    renderError: String?
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column {
            // Error indicator
            AnimatedVisibility(visible = renderError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Error.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = renderError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Error,
                        maxLines = 1
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionChip(
                    icon = Icons.Default.ContentPaste,
                    label = "粘贴",
                    onClick = onPaste,
                    highlight = true
                )
                ActionChip(
                    icon = Icons.Default.ContentCopy,
                    label = "复制",
                    onClick = onCopy
                )
                ActionChip(
                    icon = Icons.Default.Dashboard,
                    label = "模板",
                    onClick = onTemplateClick
                )
                ActionChip(
                    icon = Icons.Default.Share,
                    label = "分享",
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
private fun ActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    highlight: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateBottomSheet(
    onDismiss: () -> Unit,
    onTemplateSelected: (DiagramTemplate) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "选择模板",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(DiagramTemplate.entries) { template ->
                    TemplateCard(
                        template = template,
                        onClick = { onTemplateSelected(template) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: DiagramTemplate,
    onClick: () -> Unit
) {
    val gradientColors = when (template) {
        DiagramTemplate.FLOWCHART -> listOf(Primary, Secondary)
        DiagramTemplate.SEQUENCE -> listOf(Secondary, Tertiary)
        DiagramTemplate.CLASS -> listOf(PrimaryDark, Primary)
        DiagramTemplate.STATE -> listOf(Tertiary, TertiaryLight)
        DiagramTemplate.PIE -> listOf(Success, Secondary)
        DiagramTemplate.GANTT -> listOf(Warning, Tertiary)
        DiagramTemplate.ER -> listOf(PrimaryLight, Secondary)
        DiagramTemplate.MINDMAP -> listOf(Primary, TertiaryLight)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 140.dp, height = 100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = template.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SaveDiagramDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保存图表") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("图表名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(title.ifBlank { "未命名图表" }) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

