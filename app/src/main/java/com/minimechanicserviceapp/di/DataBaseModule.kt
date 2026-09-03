package com.minimechanicserviceapp.di

import android.content.Context
import androidx.room.Room
import com.minimechanicserviceapp.data.local.AppDatabase
import com.minimechanicserviceapp.data.local.MechanicDao
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "mini_mechanic.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideMechanicDao(database: AppDatabase): MechanicDao = database.mechanicDao()
}