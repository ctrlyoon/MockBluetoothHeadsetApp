package com.tyeng.mockbluetoothheadsetapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothHeadset.*
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class MockBluetoothHeadset(private val context: Context) : TextToSpeech.OnInitListener {

    private val TAG = "MockBluetoothHeadset"
    private var btAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var btHeadset: BluetoothHeadset
    private lateinit var audioManager: AudioManager
    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(context, this)
    }

    fun enableBluetooth() {
        if (!btAdapter.isEnabled) {
            btAdapter.enable()
        }
        btAdapter.getProfileProxy(context, mProfileListener, HEADSET)
    }

    private val mProfileListener = object : ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == HEADSET) {
                btHeadset = proxy as BluetoothHeadset
                if (btHeadset.connectedDevices.isNotEmpty()) {
                    Log.d(TAG, "Connected device found")
                    val device = btHeadset.connectedDevices[0]
                    if (device != null) {
                        Log.d(TAG, "Device name: ${device.name}")
                        if (btHeadset.getAudioState(device) == AUDIO_STATE_DISCONNECTED) {
                            btHeadset.startVoiceRecognition(device)
                            Log.d(TAG, "startVoiceRecognition")
                            Thread.sleep(3000)
                            btHeadset.stopVoiceRecognition(device)
                            Log.d(TAG, "stopVoiceRecognition")
                            tts.speak("Incoming call from ${device.name}", TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                }
            }
        }

        override fun onServiceDisconnected(profile: Int) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result: Int = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "This Language is not supported")
            }
        } else {
            Log.e(TAG, "Initialization Failed!")
        }
    }
}
