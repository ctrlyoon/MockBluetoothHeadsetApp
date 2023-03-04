package com.tyeng.mockbluetoothheadsetapp.com.tyeng.mockbluetoothheadsetapp

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

object TextToSpeechManager : TextToSpeech.OnInitListener {
    private const val TAG = "TextToSpeechManager"
    private var mTextToSpeech: TextToSpeech? = null
    fun initialize(context: Context) {
        mTextToSpeech = TextToSpeech(context, this)
    }

    fun speak(text: String) {
        if (mTextToSpeech != null && mTextToSpeech!!.isSpeaking) {
            mTextToSpeech!!.stop()
        }

        val result: Int = mTextToSpeech?.setLanguage(Locale.US) ?: TextToSpeech.ERROR
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e(TAG, "This Language is not supported")
            return
        }

        mTextToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        mTextToSpeech?.stop()
        mTextToSpeech?.shutdown()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TextToSpeech initialized.")
        } else {
            Log.e(TAG, "TextToSpeech initialization failed!")
        }
    }
}