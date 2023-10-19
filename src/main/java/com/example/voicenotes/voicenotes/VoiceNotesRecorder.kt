package com.example.voicenotes.voicenotes

import android.Manifest
import android.media.MediaRecorder
import android.os.Environment
import androidx.compose.runtime.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.lang.Exception

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceNotesRecorder(startRecording: Boolean = false, stopRecording: Boolean = false) {
    val mediaRecorder = MediaRecorder()
    val tPath = Environment.getExternalStorageDirectory().absolutePath + "/audiorecordtest.3gp"
    var isRecording by remember { mutableStateOf(false) }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    )
    when(permissionsState.allPermissionsGranted) {
        true -> {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder.setOutputFile(tPath)
        }
    }

    if (startRecording && !isRecording && permissionsState.allPermissionsGranted) {
        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            isRecording = true
        } catch (e: Exception) {
            throw Exception("Record start failure")
        }
    }

    if (stopRecording && isRecording) {
        try {
            mediaRecorder.stop()
            mediaRecorder.release()
            isRecording = false
        } catch (e: Exception) {
            throw Exception("Record stop failure")
        }
    }




    //viewModel.setMediaPlayer(mediaRecorder)

}