package com.example.voicenotes.data.room.source

import androidx.room.*
import com.example.voicenotes.VoiceNote
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceNoteDao {

    @Query("SELECT * FROM voice_note_entity ORDER BY createdAt")
    fun getAllVoiceNotes(): Flow<List<VoiceNote>>

    @Query("SELECT * FROM voice_note_entity WHERE noteId =:noteId")
    suspend fun getVoiceNoteById(noteId: String): VoiceNote

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceNote(voiceNote: VoiceNote)

    @Update
    suspend fun updateVoiceNote(voiceNote: VoiceNote)

    @Delete
    suspend fun deleteVoiceNote(voiceNote: VoiceNote)

    @Query("SELECT * FROM voice_note_entity WHERE title LIKE '%' || :text || '%'")
    suspend fun getVoiceNotesWithTitleText(text: String): List<VoiceNote>
}