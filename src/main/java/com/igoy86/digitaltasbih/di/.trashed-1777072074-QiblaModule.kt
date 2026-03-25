package com.igoy86.digitaltasbih.di

import com.igoy86.digitaltasbih.data.repository.QiblaRepositoryImpl
import com.igoy86.digitaltasbih.domain.repository.QiblaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class QiblaModule {

    @Binds
    @Singleton
    abstract fun bindQiblaRepository(
        impl: QiblaRepositoryImpl
    ): QiblaRepository
}