package com.registro.alimentario.viewmodel;

import com.registro.alimentario.repository.ComentarioRepository;
import com.registro.alimentario.repository.ProfessionalRepository;
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
public final class ProfessionalViewModel_Factory implements Factory<ProfessionalViewModel> {
  private final Provider<ProfessionalRepository> professionalRepositoryProvider;

  private final Provider<ComentarioRepository> comentarioRepositoryProvider;

  public ProfessionalViewModel_Factory(
      Provider<ProfessionalRepository> professionalRepositoryProvider,
      Provider<ComentarioRepository> comentarioRepositoryProvider) {
    this.professionalRepositoryProvider = professionalRepositoryProvider;
    this.comentarioRepositoryProvider = comentarioRepositoryProvider;
  }

  @Override
  public ProfessionalViewModel get() {
    return newInstance(professionalRepositoryProvider.get(), comentarioRepositoryProvider.get());
  }

  public static ProfessionalViewModel_Factory create(
      javax.inject.Provider<ProfessionalRepository> professionalRepositoryProvider,
      javax.inject.Provider<ComentarioRepository> comentarioRepositoryProvider) {
    return new ProfessionalViewModel_Factory(Providers.asDaggerProvider(professionalRepositoryProvider), Providers.asDaggerProvider(comentarioRepositoryProvider));
  }

  public static ProfessionalViewModel_Factory create(
      Provider<ProfessionalRepository> professionalRepositoryProvider,
      Provider<ComentarioRepository> comentarioRepositoryProvider) {
    return new ProfessionalViewModel_Factory(professionalRepositoryProvider, comentarioRepositoryProvider);
  }

  public static ProfessionalViewModel newInstance(ProfessionalRepository professionalRepository,
      ComentarioRepository comentarioRepository) {
    return new ProfessionalViewModel(professionalRepository, comentarioRepository);
  }
}
