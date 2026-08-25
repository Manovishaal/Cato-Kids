package com.catokids.app.core

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Tiny, asset-free feedback: short tones plus haptics. Keeps the APK small and
 * avoids shipping audio files, while still giving the child instant response.
 */
class SoundEffects(context: Context) {

    private var enabled = true

    private val tone: ToneGenerator? =
        runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }.getOrNull()

    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            mgr?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    fun setEnabled(value: Boolean) { enabled = value }

    fun correct() {
        play(ToneGenerator.TONE_PROP_BEEP2, 180)
        buzz(30)
    }

    fun wrong() {
        play(ToneGenerator.TONE_SUP_ERROR, 200)
        buzz(80)
    }

    fun tap() {
        play(ToneGenerator.TONE_PROP_BEEP, 70)
    }

    fun celebrate() {
        play(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
        buzz(60)
    }

    private fun play(type: Int, ms: Int) {
        if (!enabled) return
        runCatching { tone?.startTone(type, ms) }
    }

    private fun buzz(ms: Long) {
        if (!enabled) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION") vibrator?.vibrate(ms)
            }
        }
    }
}
