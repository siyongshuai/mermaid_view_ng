package com.mermaid.studio.di;

import com.mermaid.studio.data.local.DiagramDao;
import com.mermaid.studio.data.local.MermaidDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideDiagramDaoFactory implements Factory<DiagramDao> {
  private final Provider<MermaidDatabase> databaseProvider;

  public AppModule_ProvideDiagramDaoFactory(Provider<MermaidDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DiagramDao get() {
    return provideDiagramDao(databaseProvider.get());
  }

  public static AppModule_ProvideDiagramDaoFactory create(
      Provider<MermaidDatabase> databaseProvider) {
    return new AppModule_ProvideDiagramDaoFactory(databaseProvider);
  }

  public static DiagramDao provideDiagramDao(MermaidDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDiagramDao(database));
  }
}
