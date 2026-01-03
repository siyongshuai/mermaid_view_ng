package com.mermaid.studio.data.local

import androidx.room.*
import com.mermaid.studio.data.local.entity.DiagramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagramDao {
    
    @Query("SELECT * FROM diagrams ORDER BY modifiedAt DESC")
    fun getAllDiagrams(): Flow<List<DiagramEntity>>
    
    @Query("SELECT * FROM diagrams WHERE isFavorite = 1 ORDER BY modifiedAt DESC")
    fun getFavoriteDiagrams(): Flow<List<DiagramEntity>>
    
    @Query("SELECT * FROM diagrams WHERE id = :id")
    suspend fun getDiagramById(id: String): DiagramEntity?
    
    @Query("SELECT * FROM diagrams WHERE title LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' ORDER BY modifiedAt DESC")
    fun searchDiagrams(query: String): Flow<List<DiagramEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagram(diagram: DiagramEntity)
    
    @Update
    suspend fun updateDiagram(diagram: DiagramEntity)
    
    @Delete
    suspend fun deleteDiagram(diagram: DiagramEntity)
    
    @Query("DELETE FROM diagrams WHERE id = :id")
    suspend fun deleteDiagramById(id: String)
    
    @Query("UPDATE diagrams SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)
}

