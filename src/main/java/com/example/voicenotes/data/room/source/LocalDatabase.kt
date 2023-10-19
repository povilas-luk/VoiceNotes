package com.example.voicenotes.data.room.source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.voicenotes.VoiceNote

@Database(entities = [VoiceNote::class], version = 1)
abstract class LocalDatabase: RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "local_database"
    }

    abstract val voiceNoteDao: VoiceNoteDao
}