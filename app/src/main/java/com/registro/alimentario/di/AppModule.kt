package com.registro.alimentario.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.registro.alimentario.repository.AuthRepository
import com.registro.alimentario.repository.AuthRepositoryImpl
import com.registro.alimentario.repository.ComentarioRepository
import com.registro.alimentario.repository.ComentarioRepositoryImpl
import com.registro.alimentario.repository.PhotoRepository
import com.registro.alimentario.repository.PhotoRepositoryImpl
import com.registro.alimentario.repository.ProfessionalRepository
import com.registro.alimentario.repository.ProfessionalRepositoryImpl
import com.registro.alimentario.repository.RegistroRepository
import com.registro.alimentario.repository.RegistroRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindRegistroRepository(impl: RegistroRepositoryImpl): RegistroRepository
    @Binds @Singleton abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository
    @Binds @Singleton abstract fun bindProfessionalRepository(impl: ProfessionalRepositoryImpl): ProfessionalRepository
    @Binds @Singleton abstract fun bindComentarioRepository(impl: ComentarioRepositoryImpl): ComentarioRepository
}
