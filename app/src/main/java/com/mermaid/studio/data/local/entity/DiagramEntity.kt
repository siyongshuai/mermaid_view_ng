package com.mermaid.studio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mermaid.studio.domain.model.Diagram
import com.mermaid.studio.domain.model.DiagramType

@Entity(tableName = "diagrams")
data class DiagramEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val code: String,
    val diagramType: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val isFavorite: Boolean
) {
    fun toDomain(): Diagram = Diagram(
        id = id,
        title = title,
        code = code,
        diagramType = DiagramType.entries.find { it.name == diagramType } ?: DiagramType.FLOWCHART,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        isFavorite = isFavorite
    )

    companion object {
        fun fromDomain(diagram: Diagram): DiagramEntity = DiagramEntity(
            id = diagram.id,
            title = diagram.title,
            code = diagram.code,
            diagramType = diagram.diagramType.name,
            createdAt = diagram.createdAt,
            modifiedAt = diagram.modifiedAt,
            isFavorite = diagram.isFavorite
        )
    }
}

