package com.tyeng.mockbluetoothheadsetapp

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Handler
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*

class BluetoothHeadsetService : Service(), TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "BluetoothHeadsetService"
    }

    private lateinit var audioManager: AudioManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var mockBluetoothHeadset: MockBluetoothHeadset
    private lateinit var mBluetoothHeadset: BluetoothHeadset
    private var mTextToSpeech: TextToSpeech? = null
    private lateinit var ttsManager: TextToSpeechManager
    private lateinit var callManager: CallManager

    private val mHandler = Handler()
    private var mVoiceRecognitionHandler: Handler? = null

    override fun onCreate() {
        super.onCreate()

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        mockBluetoothHeadset = MockBluetoothHeadset(this)
        mockBluetoothHeadset.enableBluetooth()
        mTextToSpeech = TextToSpeech(this, this)
        ttsManager = TextToSpeechManager(this, mTextToSpeech)
        callManager = CallManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mockBluetoothHeadset.disableBluetooth()
        mTextToSpeech?.shutdown()
        callManager.unregisterCallStateListener()
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
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = proxy as BluetoothHeadset
                mBluetoothHeadset.startVoiceRecognition(mBluetoothHeadset.connectedDevices[0])
                Log.d(TAG, "startVoiceRecognition")
                mHandler.postDelayed({
                    mBluetoothHeadset.stopVoiceRecognition(mBluetoothHeadset.connectedDevices[0])
                    Log.d(TAG, "stopVoiceRecognition")
                    ttsManager.speak("Incoming call answered.")
                    callManager.answerCall()
                }, 3000)
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            Log.d(TAG, "onServiceDisconnected")
        }
    }

    private fun requestBluetoothConnectPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1
            )
        } else {
            Log.d(TAG, "Already has permission to connect Bluetooth")
        }
    }

    private fun startVoiceRecognition() {
        if (mBluetoothHeadset.getConnectedDevices().isNotEmpty()) {
            mBluetoothHeadset.startVoiceRecognition(mBluetoothHeadset.getConnectedDevices()[0])
            Log.d(TAG, "startVoiceRecognition")
        }
    }

    private fun stopVoiceRecognition() {
        if (mBluetoothHeadset.getConnectedDevices().isNotEmpty()) {
            mBluetoothHeadset.stopVoiceRecognition(mBluetoothHeadset.getConnectedDevices()[0])
            Log.d(TAG, "stopVoiceRecognition")
        }
    }

    private val mHeadsetProfileListener: BluetoothProfile.ServiceListener =
        object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HEADSET) {
                    mBluetoothHeadset = proxy as BluetoothHeadset
                    mBluetoothHeadset.connect(mBluetoothHeadset.getConnectedDevices()[0])
                    Log.d(TAG, "onServiceConnected: Bluetooth Headset profile connected.")
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == BluetoothProfile.HEADSET) {
                    mBluetoothHeadset = null
                    Log.d(TAG, "onServiceDisconnected: Bluetooth Headset profile disconnected.")
                }
            }
        }
}
