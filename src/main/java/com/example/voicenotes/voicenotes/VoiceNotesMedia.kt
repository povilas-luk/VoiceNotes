package com.example.voicenotes.voicenotes

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build

class VoiceNotesMedia(private val path: String, private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null

    private fun setupMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    private fun setupMediaPlayer(): MediaPlayer {
        return MediaPlayer()
    }

    @Throws(VoiceNotesMediaError::class)
    fun startRecording(fileName: String) {
        mediaRecorder = setupMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(getMediaFilePath(fileName))
            prepare()
            start()
        }
    }

    @Throws(VoiceNotesMediaError::class)
    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    @Throws(VoiceNotesMediaError::class)
    fun startPlaying(path: String, position: Int = 0, finishedPlaying: () -> Unit, positionSet: () -> Unit) {
        mediaPlayer = setupMediaPlayer().apply {
            setDataSource(path)
            setOnCompletionListener(MediaPlayer.OnCompletionListener {
                finishedPlaying()
            })
            setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener {
                positionSet()
                start()
            })
            prepare()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer?.seekTo(position.toLong(), MediaPlayer.SEEK_CLOSEST)
        } else {
            mediaPlayer?.seekTo(position)
        }
    }

    @Throws(VoiceNotesMediaError::class)
    fun stopPlaying(): Int {
        var position = 0
        mediaPlayer?.apply {
            stop()
            position = mediaPlayer?.currentPosition ?: 0
            release()
        }
        mediaPlayer = null
        return position
    }

    fun getCurrentPlayerPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getCurrentPlayerDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun getPlayerDurationFromPath(filePath: String): Int {
        var duration = 0
        val player: MediaPlayer = setupMediaPlayer().apply {
            setDataSource(filePath)
            prepare()
        }
        duration = player.duration
        return duration
    }

    fun getMediaFilePath(fileName: String): String {
        return "$path/$fileName.mp3"
    }


}