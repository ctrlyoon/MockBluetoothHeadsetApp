package com.tyeng.mockbluetoothheadsetapp

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private lateinit var startServiceButton: Button
    private lateinit var stopServiceButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize TTS
        tts = TextToSpeech(applicationContext) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.US
            }
        }
        startService(Intent(this, BluetoothHeadsetService::class.java))
    }

    // Play a greeting message using TTS
    fun playGreetingMessage() {
        tts.speak("Hello, how may I assist you?", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Stop TTS
    override fun onDestroy() {
        if (tts != null) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
