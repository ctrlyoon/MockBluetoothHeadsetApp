package com.tyeng.mockbluetoothheadsetapp

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class TextToSpeechManager(private val context: Context, private val textToSpeech: TextToSpeech) : UtteranceProgressListener() {

    companion object {
        private const val TAG = "TextToSpeechManager"
    }

    private var initialized = false

    init {
        textToSpeech.setOnUtteranceProgressListener(this)
    }

    override fun onStart(utteranceId: String?) {
        Log.d(TAG, "TTS onStart")
    }

    override fun onDone(utteranceId: String?) {
        Log.d(TAG, "TTS onDone")
    }

    override fun onError(utteranceId: String?) {
        Log.d(TAG, "TTS onError")
    }

    override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
        Log.d(TAG, "TTS onRangeStart")
    }

    fun speak(text: String) {
        if (!initialized) {
            textToSpeech.setOnUtteranceProgressListener(this)
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "This Language is not supported")
            } else {
                initialized = true
            }
        }
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "messageID")
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "messageID")
    }
}
