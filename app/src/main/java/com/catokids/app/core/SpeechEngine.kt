package com.catokids.app.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Text-to-speech so every prompt can be *heard* — essential when your users
 * cannot read yet. Silently no-ops if the device has no TTS engine.
 */
class SpeechEngine(context: Context) {

    private var ready = false
    private var enabled = true

    private val tts: TextToSpeech? = runCatching {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                runCatching {
                    engine?.language = Locale.UK
                    engine?.setSpeechRate(0.85f)
                    engine?.setPitch(1.15f)
                }
                ready = true
            }
        }
        engine
    }.getOrNull()

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) stop()
    }

    fun speak(text: String?) {
        if (!enabled || !ready || text.isNullOrBlank()) return
        runCatching { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString()) }
    }

    fun stop() {
        runCatching { tts?.stop() }
    }

    fun shutdown() {
        runCatching { tts?.shutdown() }
    }
}
