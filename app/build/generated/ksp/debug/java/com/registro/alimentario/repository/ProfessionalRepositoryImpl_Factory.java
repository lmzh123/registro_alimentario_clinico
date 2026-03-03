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
public final class ProfessionalRepositoryImpl_Factory implements Factory<ProfessionalRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  public ProfessionalRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider) {
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public ProfessionalRepositoryImpl get() {
    return newInstance(firestoreProvider.get());
  }

  public static ProfessionalRepositoryImpl_Factory create(
      javax.inject.Provider<FirebaseFirestore> firestoreProvider) {
    return new ProfessionalRepositoryImpl_Factory(Providers.asDaggerProvider(firestoreProvider));
  }

  public static ProfessionalRepositoryImpl_Factory create(
      Provider<FirebaseFirestore> firestoreProvider) {
    return new ProfessionalRepositoryImpl_Factory(firestoreProvider);
  }

  public static ProfessionalRepositoryImpl newInstance(FirebaseFirestore firestore) {
    return new ProfessionalRepositoryImpl(firestore);
  }
}
