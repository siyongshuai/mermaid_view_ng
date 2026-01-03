package com.mermaid.studio.data.repository

import com.mermaid.studio.data.local.DiagramDao
import com.mermaid.studio.data.local.entity.DiagramEntity
import com.mermaid.studio.domain.model.Diagram
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagramRepository @Inject constructor(
    private val diagramDao: DiagramDao
) {
    fun getAllDiagrams(): Flow<List<Diagram>> = 
        diagramDao.getAllDiagrams().map { entities ->
            entities.map { it.toDomain() }
        }
    
    fun getFavoriteDiagrams(): Flow<List<Diagram>> =
        diagramDao.getFavoriteDiagrams().map { entities ->
            entities.map { it.toDomain() }
        }
    
    fun searchDiagrams(query: String): Flow<List<Diagram>> =
        diagramDao.searchDiagrams(query).map { entities ->
            entities.map { it.toDomain() }
        }
    
    suspend fun getDiagramById(id: String): Diagram? =
        diagramDao.getDiagramById(id)?.toDomain()
    
    suspend fun saveDiagram(diagram: Diagram) {
        diagramDao.insertDiagram(DiagramEntity.fromDomain(diagram))
    }
    
    suspend fun updateDiagram(diagram: Diagram) {
        diagramDao.updateDiagram(DiagramEntity.fromDomain(diagram))
    }
    
    suspend fun deleteDiagram(id: String) {
        diagramDao.deleteDiagramById(id)
    }
    
    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        diagramDao.updateFavorite(id, isFavorite)
    }
}

