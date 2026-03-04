package com.suvojeet.safewalk.util

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the Siren and Strobe Light features to deter attackers.
 */
@Singleton
class DeterrentManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var mediaPlayer: MediaPlayer? = null
    private var strobeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    init {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize CameraManager for strobe", e)
        }
    }

    /**
     * Start the deterrent effects.
     */
    fun start(siren: Boolean, strobe: Boolean) {
        if (siren) startSiren()
        if (strobe) startStrobe()
    }

    /**
     * Stop all deterrent effects.
     */
    fun stop() {
        stopSiren()
        stopStrobe()
    }

    private fun startSiren() {
        if (mediaPlayer?.isPlaying == true) return

        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alertUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            Log.d(TAG, "Siren started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start siren", e)
        }
    }

    private fun stopSiren() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            Log.d(TAG, "Siren stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop siren", e)
        }
    }

    private fun startStrobe() {
        if (strobeJob?.isActive == true || cameraId == null) return

        strobeJob = scope.launch {
            var flashOn = false
            try {
                while (isActive) {
                    flashOn = !flashOn
                    cameraManager.setTorchMode(cameraId!!, flashOn)
                    delay(Constants.STROBE_DELAY_MS)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Strobe light error", e)
            } finally {
                // Ensure flash is off when stopped
                try {
                    cameraManager.setTorchMode(cameraId!!, false)
                } catch (ignored: Exception) {}
            }
        }
        Log.d(TAG, "Strobe started")
    }

    private fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        Log.d(TAG, "Strobe stopped")
    }

    companion object {
        private const val TAG = "DeterrentManager"
    }
}
