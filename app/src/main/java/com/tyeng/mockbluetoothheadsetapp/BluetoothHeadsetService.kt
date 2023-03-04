package com.tyeng.mockbluetoothheadsetapp

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class BluetoothHeadsetService : Service(), BluetoothProfile.ServiceListener {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothHeadset: BluetoothHeadset? = null
    private lateinit var tts: TextToSpeech
    private lateinit var audioManager: AudioManager

    companion object {
        private const val TAG = "BluetoothHeadsetService"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Initialize TTS
        tts = TextToSpeech(applicationContext) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.US
            }
        }

        // Initialize AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Register broadcast receiver for incoming calls
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.PHONE_STATE")
        registerReceiver(callReceiver, intentFilter)

        // Start BluetoothHeadsetService
        bluetoothAdapter.getProfileProxy(applicationContext, this, BluetoothProfile.HEADSET)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister broadcast receiver for incoming calls
        unregisterReceiver(callReceiver)

        // Stop TTS
        if (tts != null) {
            tts.stop()
            tts.shutdown()
        }

        // Disconnect BluetoothHeadset
        if (bluetoothAdapter != null && bluetoothHeadset != null) {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset)
            bluetoothHeadset = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // BluetoothProfile.ServiceListener methods

    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
        if (profile == BluetoothProfile.HEADSET) {
            bluetoothHeadset = proxy as BluetoothHeadset
            Log.i(TAG, "BluetoothHeadsetService connected")
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        if (profile == BluetoothProfile.HEADSET) {
            bluetoothHeadset = null
            Log.i(TAG, "BluetoothHeadsetService disconnected")
        }
    }

    // BroadcastReceiver for incoming calls

    private val callReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getStringExtra("state")

            if (state.equals("RINGING", ignoreCase = true)) {
                val phoneNumber = intent?.getStringExtra("incoming_number")
                Log.i(TAG, "Incoming call from $phoneNumber")

                // Play greeting message
                playGreetingMessage()

                // Route audio to BluetoothHeadset
                audioManager.mode = AudioManager.MODE_IN_CALL
                audioManager.isBluetoothScoOn = true
                audioManager.startBluetoothSco()
            } else if (state.equals("IDLE", ignoreCase = true)) {
                Log.i(TAG, "Call ended")

                // Stop routing audio to BluetoothHeadset
                audioManager.mode = AudioManager.MODE_NORMAL
                audioManager.stopBluetoothSco()
                audioManager.isBluetoothScoOn = false
            }
        }
    }

    // Play a greeting message using TTS
    private fun playGreetingMessage() {
        tts.speak("Hello, how may I assist you?", TextToSpeech.QUEUE_FLUSH, null, null)
    }
}
