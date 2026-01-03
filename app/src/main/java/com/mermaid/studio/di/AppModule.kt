package com.mermaid.studio.di

import android.content.Context
import androidx.room.Room
import com.mermaid.studio.data.local.DiagramDao
import com.mermaid.studio.data.local.MermaidDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MermaidDatabase {
        return Room.databaseBuilder(
            context,
            MermaidDatabase::class.java,
            "mermaid_studio.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDiagramDao(database: MermaidDatabase): DiagramDao {
        return database.diagramDao()
    }
}

