package com.example.voicenotes.data.room.repository

import androidx.room.*
import com.example.voicenotes.VoiceNote
import com.example.voicenotes.data.room.source.VoiceNoteDao
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths

class VoiceNoteRepository(
    private val dao: VoiceNoteDao
) {

    fun getAllVoiceNotes(): Flow<List<VoiceNote>> {
        return dao.getAllVoiceNotes()
    }

    suspend fun getVoiceNoteById(noteId: String): VoiceNote {
        return dao.getVoiceNoteById(noteId)
    }

    suspend fun insertVoiceNote(voiceNote: VoiceNote) {
        return dao.insertVoiceNote(voiceNote)
    }

    suspend fun updateVoiceNote(voiceNote: VoiceNote) {
        return dao.updateVoiceNote(voiceNote)
    }

    @Throws(RepositoryError::class)
    suspend fun deleteVoiceNote(voiceNote: VoiceNote) {
        val file = File(voiceNote.filePath)
        file.delete()
        return dao.deleteVoiceNote(voiceNote)
    }

    suspend fun getVoiceNotesWithTitleText(text: String): List<VoiceNote>  {
        return dao.getVoiceNotesWithTitleText(text)
    }

}