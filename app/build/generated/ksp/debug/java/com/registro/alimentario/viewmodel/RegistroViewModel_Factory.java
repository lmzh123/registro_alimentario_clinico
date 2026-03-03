package com.registro.alimentario.viewmodel;

import com.registro.alimentario.repository.PhotoRepository;
import com.registro.alimentario.repository.RegistroRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class RegistroViewModel_Factory implements Factory<RegistroViewModel> {
  private final Provider<RegistroRepository> registroRepositoryProvider;

  private final Provider<PhotoRepository> photoRepositoryProvider;

  public RegistroViewModel_Factory(Provider<RegistroRepository> registroRepositoryProvider,
      Provider<PhotoRepository> photoRepositoryProvider) {
    this.registroRepositoryProvider = registroRepositoryProvider;
    this.photoRepositoryProvider = photoRepositoryProvider;
  }

  @Override
  public RegistroViewModel get() {
    return newInstance(registroRepositoryProvider.get(), photoRepositoryProvider.get());
  }

  public static RegistroViewModel_Factory create(
      javax.inject.Provider<RegistroRepository> registroRepositoryProvider,
      javax.inject.Provider<PhotoRepository> photoRepositoryProvider) {
    return new RegistroViewModel_Factory(Providers.asDaggerProvider(registroRepositoryProvider), Providers.asDaggerProvider(photoRepositoryProvider));
  }

  public static RegistroViewModel_Factory create(
      Provider<RegistroRepository> registroRepositoryProvider,
      Provider<PhotoRepository> photoRepositoryProvider) {
    return new RegistroViewModel_Factory(registroRepositoryProvider, photoRepositoryProvider);
  }

  public static RegistroViewModel newInstance(RegistroRepository registroRepository,
      PhotoRepository photoRepository) {
    return new RegistroViewModel(registroRepository, photoRepository);
  }
}
