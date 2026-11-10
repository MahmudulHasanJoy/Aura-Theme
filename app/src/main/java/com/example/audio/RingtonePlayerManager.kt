package com.example.audio

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import com.example.data.model.RingtoneItem
import com.example.data.model.RingtoneTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

data class PlaybackState(
    val currentRingtone: RingtoneItem? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0
)

class RingtonePlayerManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun play(ringtone: RingtoneItem) {
        if (_playbackState.value.currentRingtone?.id == ringtone.id && mediaPlayer != null) {
            if (_playbackState.value.isPlaying) {
                mediaPlayer?.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
            } else {
                mediaPlayer?.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startProgressTracker()
            }
            return
        }

        stop()

        scope.launch(Dispatchers.IO) {
            try {
                val wavFile = AudioSynthEngine.getOrGenerateWavFile(context, ringtone.toneType)
                withContext(Dispatchers.Main) {
                    val player = MediaPlayer().apply {
                        setDataSource(wavFile.absolutePath)
                        prepare()
                        isLooping = true
                        start()
                    }
                    mediaPlayer = player
                    _playbackState.value = PlaybackState(
                        currentRingtone = ringtone,
                        isPlaying = true,
                        progress = 0f,
                        currentPositionMs = 0,
                        durationMs = player.duration
                    )
                    startProgressTracker()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    stop()
                }
            }
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _playbackState.value = PlaybackState()
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && mediaPlayer != null && _playbackState.value.isPlaying) {
                try {
                    val player = mediaPlayer ?: break
                    val current = player.currentPosition
                    val duration = player.duration
                    val prog = if (duration > 0) current.toFloat() / duration.toFloat() else 0f
                    _playbackState.value = _playbackState.value.copy(
                        progress = prog,
                        currentPositionMs = current,
                        durationMs = duration
                    )
                } catch (_: Exception) {}
                delay(100)
            }
        }
    }

    /**
     * Checks if the app can modify system settings (required to set system ringtone).
     */
    fun canModifySystemSettings(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    fun getSettingsPermissionIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent()
        }
    }

    suspend fun applyRingtone(
        ringtone: RingtoneItem,
        target: RingtoneTarget
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val wavFile = AudioSynthEngine.getOrGenerateWavFile(context, ringtone.toneType)

            if (!canModifySystemSettings()) {
                return@withContext Result.failure(
                    SecurityException("Permission needed: Please enable 'Modify system settings' to set device ringtones.")
                )
            }

            val ringtoneType = when (target) {
                RingtoneTarget.PHONE_CALL -> RingtoneManager.TYPE_RINGTONE
                RingtoneTarget.NOTIFICATION -> RingtoneManager.TYPE_NOTIFICATION
                RingtoneTarget.ALARM -> RingtoneManager.TYPE_ALARM
            }

            // Insert into MediaStore for proper Android ringtone registration
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "${ringtone.title}.wav")
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
                put(MediaStore.Audio.Media.TITLE, ringtone.title)
                put(MediaStore.Audio.Media.ARTIST, "Aura Themes")
                put(MediaStore.Audio.Media.IS_RINGTONE, target == RingtoneTarget.PHONE_CALL)
                put(MediaStore.Audio.Media.IS_NOTIFICATION, target == RingtoneTarget.NOTIFICATION)
                put(MediaStore.Audio.Media.IS_ALARM, target == RingtoneTarget.ALARM)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val relPath = when (target) {
                        RingtoneTarget.PHONE_CALL -> Environment.DIRECTORY_RINGTONES
                        RingtoneTarget.NOTIFICATION -> Environment.DIRECTORY_NOTIFICATIONS
                        RingtoneTarget.ALARM -> Environment.DIRECTORY_ALARMS
                    }
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relPath)
                }
            }

            val resolver = context.contentResolver
            val audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext Result.failure(Exception("Failed to register audio file in MediaStore"))

            resolver.openOutputStream(audioUri)?.use { outputStream ->
                FileInputStream(wavFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            RingtoneManager.setActualDefaultRingtoneUri(context, ringtoneType, audioUri)

            val targetName = when (target) {
                RingtoneTarget.PHONE_CALL -> "Phone Ringtone"
                RingtoneTarget.NOTIFICATION -> "Notification Sound"
                RingtoneTarget.ALARM -> "Alarm Sound"
            }

            Result.success("Set '${ringtone.title}' as $targetName successfully!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
