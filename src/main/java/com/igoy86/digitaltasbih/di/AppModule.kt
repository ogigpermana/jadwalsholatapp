package com.igoy86.digitaltasbih.di

import android.content.Context
import androidx.room.Room
import com.igoy86.digitaltasbih.data.local.DhikrDao
import com.igoy86.digitaltasbih.data.local.TasbihDao
import com.igoy86.digitaltasbih.data.local.TasbihDatabase
import com.igoy86.digitaltasbih.data.repository.DhikrRepositoryImpl
import com.igoy86.digitaltasbih.data.repository.SettingsRepositoryImpl
import com.igoy86.digitaltasbih.data.repository.TasbihRepositoryImpl
import com.igoy86.digitaltasbih.data.repository.ThemeRepositoryImpl
import com.igoy86.digitaltasbih.domain.repository.DhikrRepository
import com.igoy86.digitaltasbih.domain.repository.SettingsRepository
import com.igoy86.digitaltasbih.domain.repository.TasbihRepository
import com.igoy86.digitaltasbih.domain.repository.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TasbihDatabase =
        Room.databaseBuilder(
            context,
            TasbihDatabase::class.java,
            "tasbih.db"
        )
        .fallbackToDestructiveMigration()
        .allowMainThreadQueries()
        .build()

    @Provides
    @Singleton
    fun provideDao(database: TasbihDatabase): TasbihDao =
        database.tasbihDao()

    @Provides
    @Singleton
    fun provideDhikrDao(database: TasbihDatabase): DhikrDao =
        database.dhikrDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTasbihRepository(
        impl: TasbihRepositoryImpl
    ): TasbihRepository

    @Binds
    @Singleton
    abstract fun bindDhikrRepository(
        impl: DhikrRepositoryImpl
    ): DhikrRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        impl: ThemeRepositoryImpl
    ): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
