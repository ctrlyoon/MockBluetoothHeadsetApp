package com.tyeng.mockbluetoothheadsetapp

import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log

class CallManager(private val context: Context, private val headset: BluetoothHeadset) :
    BluetoothHeadset.ServiceListener {

    companion object {
        private const val TAG = "CallManager"
    }

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler: Handler = Handler(Looper.getMainLooper())

    fun start() {
        headset.startVoiceRecognition(headset.connectedDevices[0])
        Log.d(TAG, "startVoiceRecognition")
        handler.postDelayed({
            stop()
        }, 3000)
    }

    fun stop() {
        headset.stopVoiceRecognition(headset.connectedDevices[0])
        Log.d(TAG, "stopVoiceRecognition")
    }

    override fun onServiceConnected(profile: Int, proxy: BluetoothHeadset) {
        if (profile == BluetoothHeadset.HEADSET) {
            this.headset.startVoiceRecognition(this.headset.connectedDevices[0])
            Log.d(TAG, "startVoiceRecognition")
            handler.postDelayed({
                this.headset.stopVoiceRecognition(this.headset.connectedDevices[0])
                Log.d(TAG, "stopVoiceRecognition")
            }, 3000)
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        // Do nothing
    }
}
