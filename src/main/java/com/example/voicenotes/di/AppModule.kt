package com.example.voicenotes.di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import androidx.room.Room
import com.example.voicenotes.MainActivity
import com.example.voicenotes.data.room.repository.VoiceNoteRepository
import com.example.voicenotes.data.room.source.LocalDatabase
import com.example.voicenotes.voicenotes.VoiceNotesMedia
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocalDatabase(app: Application): LocalDatabase {
        return Room.databaseBuilder(
            app,
            LocalDatabase::class.java,
            LocalDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideVoiceNoteRepository(database: LocalDatabase): VoiceNoteRepository {
        return VoiceNoteRepository(database.voiceNoteDao)
    }

    @Provides
    @Singleton
    fun provideVoiceNotesMedia(app: Application): VoiceNotesMedia {
        val file = ContextWrapper(app.applicationContext).getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val path = file?.path ?: ""
        return VoiceNotesMedia(path, app.applicationContext)
    }


}