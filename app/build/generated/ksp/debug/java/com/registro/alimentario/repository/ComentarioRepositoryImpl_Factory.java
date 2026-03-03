package com.registro.alimentario.repository;

import com.google.firebase.auth.FirebaseAuth;
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
public final class ComentarioRepositoryImpl_Factory implements Factory<ComentarioRepositoryImpl> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseAuth> authProvider;

  public ComentarioRepositoryImpl_Factory(Provider<FirebaseFirestore> firestoreProvider,
      Provider<FirebaseAuth> authProvider) {
    this.firestoreProvider = firestoreProvider;
    this.authProvider = authProvider;
  }

  @Override
  public ComentarioRepositoryImpl get() {
    return newInstance(firestoreProvider.get(), authProvider.get());
  }

  public static ComentarioRepositoryImpl_Factory create(
      javax.inject.Provider<FirebaseFirestore> firestoreProvider,
      javax.inject.Provider<FirebaseAuth> authProvider) {
    return new ComentarioRepositoryImpl_Factory(Providers.asDaggerProvider(firestoreProvider), Providers.asDaggerProvider(authProvider));
  }

  public static ComentarioRepositoryImpl_Factory create(
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseAuth> authProvider) {
    return new ComentarioRepositoryImpl_Factory(firestoreProvider, authProvider);
  }

  public static ComentarioRepositoryImpl newInstance(FirebaseFirestore firestore,
      FirebaseAuth auth) {
    return new ComentarioRepositoryImpl(firestore, auth);
  }
}
