package com.mermaid.studio.ui.screens;

import android.content.Context;
import com.mermaid.studio.data.repository.DiagramRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class EditorViewModel_Factory implements Factory<EditorViewModel> {
  private final Provider<DiagramRepository> repositoryProvider;

  private final Provider<Context> contextProvider;

  public EditorViewModel_Factory(Provider<DiagramRepository> repositoryProvider,
      Provider<Context> contextProvider) {
    this.repositoryProvider = repositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public EditorViewModel get() {
    return newInstance(repositoryProvider.get(), contextProvider.get());
  }

  public static EditorViewModel_Factory create(Provider<DiagramRepository> repositoryProvider,
      Provider<Context> contextProvider) {
    return new EditorViewModel_Factory(repositoryProvider, contextProvider);
  }

  public static EditorViewModel newInstance(DiagramRepository repository, Context context) {
    return new EditorViewModel(repository, context);
  }
}
