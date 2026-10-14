package com.jarvis.ai.voice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class Voice(private val ctx: Context, private val onText: (String) -> Unit, private val onFail: (String) -> Unit,
            private val onLevel: (Float) -> Unit) {
    private var sr: SpeechRecognizer? = null
    private val tts = TextToSpeech(ctx) { }
    fun listen() {
        if (!SpeechRecognizer.isRecognitionAvailable(ctx)) { onFail("Speech recognition not available"); return }
        sr?.destroy()
        val r = SpeechRecognizer.createSpeechRecognizer(ctx); sr = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onResults(b: Bundle) { b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(onText) }
            override fun onError(error: Int) { onLevel(0f); onFail("Voice error $error") }
            override fun onRmsChanged(v: Float) { onLevel(v.coerceIn(0f, 10f) / 10f) }
            override fun onReadyForSpeech(p: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() { onLevel(0f) }
            override fun onPartialResults(p: Bundle?) {}
            override fun onEvent(t: Int, p: Bundle?) {}
        })
        r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN"))
    }
    fun speak(t: String) {
        if (t.isBlank()) return
        tts.language = Locale("en", "IN"); tts.speak(t, TextToSpeech.QUEUE_FLUSH, null, "j")
    }
    fun release() { sr?.destroy(); tts.shutdown() }
}
