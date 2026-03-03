package com.registro.alimentario.repository;

import com.google.firebase.storage.FirebaseStorage;
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
public final class PhotoRepositoryImpl_Factory implements Factory<PhotoRepositoryImpl> {
  private final Provider<FirebaseStorage> storageProvider;

  public PhotoRepositoryImpl_Factory(Provider<FirebaseStorage> storageProvider) {
    this.storageProvider = storageProvider;
  }

  @Override
  public PhotoRepositoryImpl get() {
    return newInstance(storageProvider.get());
  }

  public static PhotoRepositoryImpl_Factory create(
      javax.inject.Provider<FirebaseStorage> storageProvider) {
    return new PhotoRepositoryImpl_Factory(Providers.asDaggerProvider(storageProvider));
  }

  public static PhotoRepositoryImpl_Factory create(Provider<FirebaseStorage> storageProvider) {
    return new PhotoRepositoryImpl_Factory(storageProvider);
  }

  public static PhotoRepositoryImpl newInstance(FirebaseStorage storage) {
    return new PhotoRepositoryImpl(storage);
  }
}
