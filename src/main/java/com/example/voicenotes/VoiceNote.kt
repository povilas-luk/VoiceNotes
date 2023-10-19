package com.example.voicenotes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_note_entity")
data class VoiceNote(
    @PrimaryKey val noteId: String = "",
    val title: String = "",
    val createdAt: Long = 0L,
    val duration: Int = 0,
    val filePath: String = "",
    val currentPosition: Int = 0,
)
