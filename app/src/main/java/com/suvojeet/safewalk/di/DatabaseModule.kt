package com.suvojeet.safewalk.di

import android.content.Context
import androidx.room.Room
import com.suvojeet.safewalk.data.local.db.SafeWalkDatabase
import com.suvojeet.safewalk.data.local.db.dao.ContactDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): SafeWalkDatabase = Room.databaseBuilder(
        context,
        SafeWalkDatabase::class.java,
        "safewalk_db",
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideContactDao(database: SafeWalkDatabase): ContactDao = database.contactDao()
}
