package com.tyeng.mockbluetoothheadsetapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothHeadset.*
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class BluetoothHeadsetService : TextToSpeech.OnInitListener {
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mBluetoothHeadset: BluetoothHeadset
    private var mTextToSpeech: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mTextToSpeech = TextToSpeech(this, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        if (mBluetoothAdapter.isEnabled && !mBluetoothAdapter.isDiscovering) {
            Log.d(TAG, "start discovery")
            mBluetoothAdapter.startDiscovery()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mTextToSpeech?.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result: Int = mTextToSpeech?.setLanguage(Locale.US) ?: TextToSpeech.ERROR
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "This Language is not supported")
            }
        } else {
            Log.e(TAG, "Initialization Failed!")
        }
    }

    private val mProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            Log.d(TAG, "onServiceConnected")
            if (profile == HEADSET) {
                mBluetoothHeadset = proxy as BluetoothHeadset
                mBluetoothHeadset.startVoiceRecognition(mBluetoothHeadset.connectedDevices[0])
                Log.d(TAG, "startVoiceRecognition")
                Thread.sleep(3000)
                mBluetoothHeadset.stopVoiceRecognition(mBluetoothHeadset.connectedDevices[0])
                Log.d(TAG, "stopVoiceRecognition")
                mTextToSpeech?.speak("Incoming call from John Smith", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            Log.d(TAG, "onServiceDisconnected")
        }
    }

    private val mBluetoothStateListener = object : BluetoothAdapter.StateListener {
        override fun onStateOff() {
            Log.d(TAG, "Bluetooth off")
        }

        override fun onStateOn() {
            Log.d(TAG, "Bluetooth on")
            mBluetoothAdapter.getProfileProxy(this@BluetoothHeadsetService, mProfileListener, HEADSET)
        }
    }

    companion object {
        private const val TAG = "BluetoothHeadsetService"
    }

    init {
        mBluetoothAdapter.getProfileProxy(this@BluetoothHeadsetService, mProfileListener, HEADSET)
        mBluetoothAdapter.addStateListener(mBluetoothStateListener)
    }
}
