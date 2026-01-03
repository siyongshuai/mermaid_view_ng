package com.mermaid.studio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mermaid.studio.data.local.entity.DiagramEntity

@Database(
    entities = [DiagramEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MermaidDatabase : RoomDatabase() {
    abstract fun diagramDao(): DiagramDao
}

