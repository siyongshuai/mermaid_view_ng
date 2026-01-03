package com.mermaid.studio.domain.model

import java.util.UUID

/**
 * Domain model representing a Mermaid diagram
 */
data class Diagram(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled",
    val code: String = DEFAULT_CODE,
    val diagramType: DiagramType = DiagramType.FLOWCHART,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    companion object {
        const val DEFAULT_CODE = """graph TD
    A[开始] --> B{判断条件}
    B -->|是| C[执行操作A]
    B -->|否| D[执行操作B]
    C --> E[结束]
    D --> E"""
    }
}

/**
 * Supported Mermaid diagram types
 */
enum class DiagramType(val displayName: String, val prefix: String) {
    FLOWCHART("流程图", "graph"),
    SEQUENCE("时序图", "sequenceDiagram"),
    CLASS_DIAGRAM("类图", "classDiagram"),
    STATE_DIAGRAM("状态图", "stateDiagram-v2"),
    ER_DIAGRAM("ER图", "erDiagram"),
    GANTT("甘特图", "gantt"),
    PIE("饼图", "pie"),
    MINDMAP("思维导图", "mindmap"),
    TIMELINE("时间线", "timeline"),
    JOURNEY("用户旅程", "journey"),
    GIT_GRAPH("Git图", "gitGraph"),
    C4_CONTEXT("C4架构图", "C4Context");

    companion object {
        fun fromCode(code: String): DiagramType {
            val trimmed = code.trim().lowercase()
            return entries.find { trimmed.startsWith(it.prefix.lowercase()) } ?: FLOWCHART
        }
    }
}

/**
 * Editor state for the current editing session
 */
data class EditorState(
    val code: String = Diagram.DEFAULT_CODE,
    val isEditorReady: Boolean = false,
    val isRendering: Boolean = false,
    val renderError: String? = null,
    val lastSvg: String? = null,
    val isDirty: Boolean = false
)

