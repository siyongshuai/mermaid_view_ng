package com.mermaid.studio.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mermaid.studio.data.repository.DiagramRepository
import com.mermaid.studio.domain.model.Diagram
import com.mermaid.studio.domain.model.DiagramType
import com.mermaid.studio.domain.model.EditorState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: DiagramRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    private val _currentDiagram = MutableStateFlow<Diagram?>(null)
    val currentDiagram: StateFlow<Diagram?> = _currentDiagram.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // 撤销/重做栈
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private var lastCode: String = ""

    private val clipboardManager: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    fun onCodeChanged(code: String) {
        // 保存到撤销栈
        if (lastCode.isNotEmpty() && lastCode != code) {
            undoStack.add(lastCode)
            if (undoStack.size > 50) undoStack.removeAt(0) // 限制栈大小
            redoStack.clear()
        }
        lastCode = code
        
        _editorState.update { 
            it.copy(
                code = code,
                isDirty = true
            )
        }
    }

    fun onRenderSuccess(svg: String) {
        _editorState.update { 
            it.copy(
                isRendering = false,
                renderError = null,
                lastSvg = svg
            )
        }
    }

    fun onRenderError(error: String) {
        _editorState.update { 
            it.copy(
                isRendering = false,
                renderError = error
            )
        }
    }

    fun loadDiagram(id: String) {
        viewModelScope.launch {
            repository.getDiagramById(id)?.let { diagram ->
                _currentDiagram.value = diagram
                lastCode = diagram.code
                _editorState.update { 
                    it.copy(
                        code = diagram.code,
                        isDirty = false
                    )
                }
            }
        }
    }

    fun saveDiagram(title: String? = null) {
        viewModelScope.launch {
            val state = _editorState.value
            val current = _currentDiagram.value
            
            val diagram = current?.copy(
                title = title ?: current.title,
                code = state.code,
                diagramType = DiagramType.fromCode(state.code),
                modifiedAt = System.currentTimeMillis()
            ) ?: Diagram(
                title = title ?: "未命名图表",
                code = state.code,
                diagramType = DiagramType.fromCode(state.code)
            )

            repository.saveDiagram(diagram)
            _currentDiagram.value = diagram
            _editorState.update { it.copy(isDirty = false) }
            _toastMessage.emit("保存成功")
        }
    }

    fun newDiagram() {
        _currentDiagram.value = null
        lastCode = Diagram.DEFAULT_CODE
        undoStack.clear()
        redoStack.clear()
        _editorState.update { 
            EditorState(code = Diagram.DEFAULT_CODE)
        }
    }

    fun loadTemplate(template: DiagramTemplate) {
        undoStack.add(_editorState.value.code)
        redoStack.clear()
        lastCode = template.code
        _editorState.update { 
            it.copy(
                code = template.code,
                isDirty = true
            )
        }
    }

    /**
     * 撤销
     */
    fun undo() {
        if (undoStack.isNotEmpty()) {
            val current = _editorState.value.code
            redoStack.add(current)
            val previous = undoStack.removeLast()
            lastCode = previous
            _editorState.update { it.copy(code = previous, isDirty = true) }
        }
    }

    /**
     * 重做
     */
    fun redo() {
        if (redoStack.isNotEmpty()) {
            val current = _editorState.value.code
            undoStack.add(current)
            val next = redoStack.removeLast()
            lastCode = next
            _editorState.update { it.copy(code = next, isDirty = true) }
        }
    }

    /**
     * 从系统剪贴板粘贴内容（追加到代码末尾）
     */
    fun pasteFromClipboard() {
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text?.toString() ?: return
            val currentCode = _editorState.value.code
            // 将粘贴的内容添加到末尾
            val newCode = if (currentCode.isBlank()) {
                text
            } else {
                "$currentCode\n$text"
            }
            onCodeChanged(newCode)
            viewModelScope.launch {
                _toastMessage.emit("已粘贴")
            }
        } else {
            viewModelScope.launch {
                _toastMessage.emit("剪贴板为空")
            }
        }
    }

    /**
     * 复制当前代码到剪贴板
     */
    fun copyAllToClipboard() {
        val code = _editorState.value.code
        val clip = ClipData.newPlainText("Mermaid Code", code)
        clipboardManager.setPrimaryClip(clip)
        viewModelScope.launch {
            _toastMessage.emit("已复制到剪贴板")
        }
    }

    /**
     * 清空编辑器内容
     */
    fun clearEditor() {
        undoStack.add(_editorState.value.code)
        redoStack.clear()
        lastCode = ""
        _editorState.update { it.copy(code = "", isDirty = true) }
    }
}

/**
 * Diagram templates for quick start
 */
enum class DiagramTemplate(val displayName: String, val code: String) {
    FLOWCHART("流程图", """graph TD
    A[开始] --> B{条件判断}
    B -->|是| C[执行操作]
    B -->|否| D[其他操作]
    C --> E[结束]
    D --> E"""),
    
    SEQUENCE("时序图", """sequenceDiagram
    participant A as 用户
    participant B as 系统
    participant C as 数据库
    A->>B: 发送请求
    B->>C: 查询数据
    C-->>B: 返回结果
    B-->>A: 响应数据"""),
    
    CLASS("类图", """classDiagram
    class Animal {
        +String name
        +int age
        +makeSound()
    }
    class Dog {
        +bark()
    }
    class Cat {
        +meow()
    }
    Animal <|-- Dog
    Animal <|-- Cat"""),
    
    STATE("状态图", """stateDiagram-v2
    [*] --> 待处理
    待处理 --> 处理中: 开始处理
    处理中 --> 已完成: 处理成功
    处理中 --> 失败: 处理失败
    失败 --> 待处理: 重试
    已完成 --> [*]"""),
    
    PIE("饼图", """pie title 项目时间分配
    "开发" : 45
    "测试" : 25
    "文档" : 15
    "会议" : 15"""),
    
    GANTT("甘特图", """gantt
    title 项目计划
    dateFormat YYYY-MM-DD
    section 设计
    需求分析    :a1, 2024-01-01, 7d
    系统设计    :after a1, 5d
    section 开发
    前端开发    :2024-01-15, 14d
    后端开发    :2024-01-15, 14d
    section 测试
    集成测试    :2024-02-01, 7d"""),
    
    ER("ER图", """erDiagram
    USER ||--o{ ORDER : places
    ORDER ||--|{ LINE_ITEM : contains
    PRODUCT ||--o{ LINE_ITEM : included_in
    
    USER {
        int id PK
        string name
        string email
    }
    ORDER {
        int id PK
        date created_at
        int user_id FK
    }"""),
    
    MINDMAP("思维导图", """mindmap
  root((项目管理))
    计划
      需求分析
      资源规划
      时间安排
    执行
      开发
      测试
      部署
    监控
      进度跟踪
      风险管理
      质量控制""")
}
