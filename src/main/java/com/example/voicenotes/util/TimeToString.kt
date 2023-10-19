package com.example.voicenotes.util

import java.lang.Exception

fun timeMillsToString(millis: Long): String {
    return try {
        val buffer = StringBuffer()
        val hours = (millis / (1000 * 60 * 60)).toInt()
        val minutes = (millis % (1000 * 60 * 60) / (1000 * 60)).toInt()
        val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        if (hours < 1) { buffer
                .append(String.format("%02d", hours))
                .append(":") }
        buffer
            .append(String.format("%02d", minutes))
            .append(":")
            .append(String.format("%02d", seconds))
        buffer.toString()
    } catch (e: Exception) {
        "00:00"
    }

}