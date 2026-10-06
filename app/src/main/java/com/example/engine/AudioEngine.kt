package com.example.engine

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.example.model.AudioClip
import java.io.File
import java.util.UUID

class AudioEngine(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var mediaPlayer: MediaPlayer? = null

    fun startVoiceOverRecording(): File? {
        return try {
            val file = File(context.cacheDir, "voiceover_${UUID.randomUUID()}.m4a")
            currentRecordingFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            file
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to start audio recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            null
        }
    }

    fun stopVoiceOverRecording(): File? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            currentRecordingFile
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error stopping recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            currentRecordingFile
        }
    }

    fun generateBeatMarkers(durationMs: Long, bpm: Int = 128): List<Long> {
        val intervalMs = (60_000L / bpm).coerceAtLeast(200L)
        val markers = mutableListOf<Long>()
        var cur = intervalMs
        while (cur < durationMs) {
            markers.add(cur)
            cur += intervalMs
        }
        return markers
    }

    fun extractAudioFromVideoClip(title: String, durationMs: Long): AudioClip {
        return AudioClip(
            id = UUID.randomUUID().toString(),
            title = "Extracted Audio ($title)",
            startMs = 0L,
            durationMs = durationMs,
            trimInMs = 0L,
            trimOutMs = durationMs,
            waveforms = List(24) { (it % 7 + 2) / 10f },
            beatMarkersMs = generateBeatMarkers(durationMs, 124)
        )
    }

    fun release() {
        try {
            mediaRecorder?.release()
            mediaPlayer?.release()
        } catch (_: Exception) {}
    }
}
