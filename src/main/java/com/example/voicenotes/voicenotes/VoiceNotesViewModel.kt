package com.example.voicenotes.voicenotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicenotes.VoiceNote
import com.example.voicenotes.data.room.repository.RepositoryError
import com.example.voicenotes.data.room.repository.VoiceNoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class VoiceNotesViewModel @Inject constructor(
    private val repository: VoiceNoteRepository,
    private val voiceNotesMedia: VoiceNotesMedia,
): ViewModel() {

    sealed class VoiceNotesEvent {
        object StartRecording : VoiceNotesEvent()
        object StopRecording : VoiceNotesEvent()
        data class StartPlaying(val noteId: String) : VoiceNotesEvent()
        data class StopPlaying(val noteId: String) : VoiceNotesEvent()
        data class DeleteVoiceNote(val voiceNote: VoiceNote) : VoiceNotesEvent()
        data class SearchVoiceNotesList(val text: String) : VoiceNotesEvent()
        object Reload : VoiceNotesEvent()
    }

    data class VoiceNotesState(
        val loading: Boolean = false,
        val voiceNotesList: List<VoiceNote> = emptyList(),
        val currentlyPlayingVoiceNoteId: String? = null,
        val currentVoiceNoteRecordingPosition: Int = 0,
        val currentlyRecording: Boolean = false,
        val error: String? = null,
    )

    private val _voiceNotesState = MutableStateFlow(VoiceNotesState(loading = true))
    val voiceNotesState = _voiceNotesState.asStateFlow()

    private var getAllVoiceNotesJob: Job? = null
    private var playingRecord: Job? = null

    private var currentVoiceNoteRecording: VoiceNote? = null

    init {
        reloadData()
    }

    private fun reloadData() {
        getAllVoiceNotesJob?.cancel()

        getAllVoiceNotesJob = viewModelScope.launch {
            try {
                repository.getAllVoiceNotes().collect {
                    _voiceNotesState.value = VoiceNotesState(voiceNotesList = it)
                }
            } catch (e: Exception) {
                _voiceNotesState.value = VoiceNotesState(error = e.toString())
            }
        }
    }

    fun onEvent(event: VoiceNotesEvent) {
        when (event) {
            is VoiceNotesEvent.StartRecording -> {
                try {
                    val createdAt = System.currentTimeMillis()
                    val noteFileName = "note_$createdAt"
                    voiceNotesMedia.startRecording(noteFileName)
                    currentVoiceNoteRecording = VoiceNote(
                        noteId = UUID.randomUUID().toString(),
                        title = "Note",
                        createdAt = createdAt,
                        filePath = voiceNotesMedia.getMediaFilePath(noteFileName)
                    )
                    _voiceNotesState.update {
                        it.copy (
                            currentlyRecording = true
                        )
                    }
                } catch (e: Exception) {
                    _voiceNotesState.value = VoiceNotesState(error = e.toString())
                }
            }
            is VoiceNotesEvent.StopRecording -> {
                try {
                    voiceNotesMedia.stopRecording()
                    _voiceNotesState.update {
                        it.copy (
                            currentlyRecording = false
                        )
                    }
                    currentVoiceNoteRecording?.let {
                        viewModelScope.launch {
                            repository.insertVoiceNote(
                                it.copy(
                                    duration = voiceNotesMedia.getPlayerDurationFromPath(it.filePath),
                                )
                            )
                        }

                    }
                    currentVoiceNoteRecording = null
                } catch (e: Exception) {
                    _voiceNotesState.value = VoiceNotesState(error = e.toString())
                }
            }
            is VoiceNotesEvent.DeleteVoiceNote -> {
                viewModelScope.launch {
                    try {
                        repository.deleteVoiceNote(event.voiceNote)
                    } catch (e: RepositoryError) {
                        _voiceNotesState.update { it.copy(error = "Failed to delete voice note") }
                    }
                }
            }
            is VoiceNotesEvent.SearchVoiceNotesList -> {
                viewModelScope.launch {
                    try {
                        _voiceNotesState.update {
                            it.copy(
                                voiceNotesList = repository.getVoiceNotesWithTitleText(event.text)
                            )
                        }
                    } catch (e: Exception) {
                        _voiceNotesState.value = VoiceNotesState(error = e.toString())
                    }
                }
            }
            is VoiceNotesEvent.StartPlaying -> {
                viewModelScope.launch {
                    try {
                        _voiceNotesState.value.voiceNotesList.find { it.noteId == event.noteId }?.let { voiceNote ->
                            voiceNotesMedia.startPlaying(
                                path = voiceNote.filePath,
                                position = voiceNote.currentPosition,
                                finishedPlaying = {  finishedPlaying(voiceNote = voiceNote) },
                                positionSet = {
                                    _voiceNotesState.update {
                                        it.copy(
                                            currentlyPlayingVoiceNoteId = voiceNote.noteId
                                        )
                                    }
                                    playingTimer()
                                }
                            )
                        }
                    } catch (e: Exception) {
                        _voiceNotesState.value = VoiceNotesState(error = e.toString())
                    }
                }
            }
            is VoiceNotesEvent.StopPlaying -> {
                viewModelScope.launch {
                    try {
                        _voiceNotesState.value.voiceNotesList.find { it.noteId == event.noteId }?.let { voiceNote ->
                            if (_voiceNotesState.value.currentlyPlayingVoiceNoteId == voiceNote.noteId) {
                                val position = voiceNotesMedia.stopPlaying()
                                finishedPlaying(voiceNote, position)
                            }
                        }
                    } catch (e: Exception) {
                        _voiceNotesState.value = VoiceNotesState(error = e.toString())
                    }
                }
            }
            is VoiceNotesEvent.Reload -> {
                reloadData()
            }
        }
    }

    private fun finishedPlaying(voiceNote: VoiceNote, position: Int = 0) {
        try {
            playingRecord?.cancel()
            viewModelScope.launch {
                repository.updateVoiceNote(
                    voiceNote = voiceNote.copy(
                        currentPosition = position
                    )
                )
                _voiceNotesState.update {
                    it.copy(
                        currentlyPlayingVoiceNoteId = null
                    )
                }
            }
        } catch (e: Exception) {
            _voiceNotesState.value = VoiceNotesState(error = e.toString())
        }
    }

    private fun playingTimer() {
        playingRecord = viewModelScope.launch() {
            while (_voiceNotesState.value.currentlyPlayingVoiceNoteId != null) {
                _voiceNotesState.update { voiceNotesState ->
                    voiceNotesState.copy(
                        currentVoiceNoteRecordingPosition = voiceNotesMedia.getCurrentPlayerPosition()
                    )
                }
                delay(10)
            }
        }

    }



}