package com.registro.alimentario.repository;

import com.google.firebase.firestore.FirebaseFirestore;
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
public final class RegistroRepositoryImpl_Factory implements Factory<RegistroRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<PhotoRepository> photoRepositoryProvider;

  public RegistroRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider,
      Provider<PhotoRepository> photoRepositoryProvider) {
    this.firestoreProvider = firestoreProvider;
    this.photoRepositoryProvider = photoRepositoryProvider;
  }

  @Override
  public RegistroRepositoryImpl get() {
    return newInstance(firestoreProvider.get(), photoRepositoryProvider.get());
  }

  public static RegistroRepositoryImpl_Factory create(
      javax.inject.Provider<FirebaseFirestore> firestoreProvider,
      javax.inject.Provider<PhotoRepository> photoRepositoryProvider) {
    return new RegistroRepositoryImpl_Factory(Providers.asDaggerProvider(firestoreProvider), Providers.asDaggerProvider(photoRepositoryProvider));
  }

  public static RegistroRepositoryImpl_Factory create(Provider<FirebaseFirestore> firestoreProvider,
      Provider<PhotoRepository> photoRepositoryProvider) {
    return new RegistroRepositoryImpl_Factory(firestoreProvider, photoRepositoryProvider);
  }

  public static RegistroRepositoryImpl newInstance(FirebaseFirestore firestore,
      PhotoRepository photoRepository) {
    return new RegistroRepositoryImpl(firestore, photoRepository);
  }
}
