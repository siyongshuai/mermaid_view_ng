package com.mermaid.studio.data.repository;

import com.mermaid.studio.data.local.DiagramDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class DiagramRepository_Factory implements Factory<DiagramRepository> {
  private final Provider<DiagramDao> diagramDaoProvider;

  public DiagramRepository_Factory(Provider<DiagramDao> diagramDaoProvider) {
    this.diagramDaoProvider = diagramDaoProvider;
  }

  @Override
  public DiagramRepository get() {
    return newInstance(diagramDaoProvider.get());
  }

  public static DiagramRepository_Factory create(Provider<DiagramDao> diagramDaoProvider) {
    return new DiagramRepository_Factory(diagramDaoProvider);
  }

  public static DiagramRepository newInstance(DiagramDao diagramDao) {
    return new DiagramRepository(diagramDao);
  }
}
