package com.tyeng.mockbluetoothheadsetapp

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
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

    override fun onCreate() {
        super.onCreate()

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        mockBluetoothHeadset = MockBluetoothHeadset(this)
        mockBluetoothHeadset.enableBluetooth()
        mTextToSpeech = TextToSpeech(this, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mockBluetoothHeadset.disableBluetooth()
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
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = proxy as BluetoothHeadset
                mBluetoothHeadset.startVoiceRecognition(mBluetoothHeadset.connectedDevices[0])
                Log.d(TAG, "startVoiceRecognition")
                Thread.sleep(3000)
                mBluetoothHeadset.stopVoiceRecognition(mBluetoothHeadset.connectedDevices[0])
                Log.d(TAG, "stopVoiceRecognition")
                mTextToSpeech?.speak("Incoming call answered.", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            Log.d(TAG, "onServiceDisconnected")
        }
    }

    private val mBluetoothStateListener = object : BluetoothAdapter.StateChangedListener() {
        override fun onStateChanged(adapter: BluetoothAdapter?, state: Int) {
            if (state == BluetoothAdapter.STATE_OFF) {
                Log.d(TAG, "Bluetooth off")
            } else if (state == BluetoothAdapter.STATE_ON) {
                Log.d(TAG, "Bluetooth on")
                btAdapter.getProfileProxy(this@BluetoothHeadsetService, mProfileListener, BluetoothProfile.HEADSET)
            }
        }
    }

    init {
        btAdapter.getProfileProxy(this@BluetoothHeadsetService, mProfileListener, BluetoothProfile.HEADSET)
        btAdapter.addStateChangedListener(mBluetoothStateListener)

        // Auto answer incoming call
        audioManager.mode = AudioManager.MODE_IN_CALL
        audioManager.isSpeakerphoneOn = true
        val thread = Thread {
            Thread.sleep(3000)
            audioManager.isMicrophoneMute = false
            audioManager.isSpeakerphoneOn = true
            try {
                val downKeyCode = KeyEvent.KEYCODE_HEADSETHOOK
                val down = KeyEvent(KeyEvent.ACTION_DOWN, downKeyCode)
                val up = KeyEvent(KeyEvent.ACTION_UP, downKeyCode)
                val keyEvent = arrayOf(down, up)
                for (i in keyEvent.indices) {
                    Thread.sleep(100)
                    audioManager.dispatchMediaKeyEvent(keyEvent[i])
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }
}
