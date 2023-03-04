package com.tyeng.mockbluetoothheadsetapp

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*

class BluetoothHeadsetService : Service(), TextToSpeech.OnInitListener, PhoneStateListener {

    companion object {
        private const val TAG = "BluetoothHeadsetService"
    }

    private lateinit var audioManager: AudioManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var mockBluetoothHeadset: MockBluetoothHeadset
    private lateinit var mBluetoothHeadset: BluetoothHeadset
    private lateinit var telephonyManager: TelephonyManager
    private var mTextToSpeech: TextToSpeech? = null
    private lateinit var ttsManager: TextToSpeechManager

    override fun onCreate() {
        super.onCreate()

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        mockBluetoothHeadset = MockBluetoothHeadset(this)
        mockBluetoothHeadset.enableBluetooth()
        mTextToSpeech = TextToSpeech(this, this)
        ttsManager = TextToSpeechManager(this, mTextToSpeech!!)
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(this, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mockBluetoothHeadset.disableBluetooth()
        mTextToSpeech?.shutdown()
        telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE)
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

    private val mHeadsetProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = proxy as BluetoothHeadset
                if (ActivityCompat.checkSelfPermission(applicationContext,BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                if (mBluetoothHeadset.connectedDevices.isNotEmpty()) {
                    val device = mBluetoothHeadset.connectedDevices[0]
                    setActiveDevice(device)
                    Log.d(TAG, "Active device set to ${device.name}")
                }
            }
        }
        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null
                Log.d(TAG, "Headset disconnected")
            }
        }
    }

    private fun connectToHeadset() {
        if (!btAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not enabled")
            return
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if (mBluetoothHeadset != null && mBluetoothHeadset.connectedDevices.isNotEmpty()) {
            Log.d(TAG, "Already connected to a headset")
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = btAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            if (btAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Headset is already connected")
                return
            }

            if (btAdapter.getProfileConnectionState(BluetoothProfile.A2DP) == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "A2DP is already connected")
                return
            }

            if (btAdapter.getProfileConnectionState(BluetoothProfile.HEALTH) == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Health is already connected")
                return
            }

            if (device.bluetoothClass.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO) {
                btAdapter.getProfileProxy(this, mHeadsetProfileListener, BluetoothProfile.HEADSET)
            }
        }
    }

    private fun setActiveDevice(device: BluetoothDevice) {
        try {
            val setPriorityMethod = mBluetoothHeadset.javaClass.getMethod(
                "setPriority",
                BluetoothDevice::class.java,
                Int::class.javaPrimitiveType
            )
            setPriorityMethod.invoke(mBluetoothHeadset, device, 100)
            val setActiveDeviceMethod = mBluetoothHeadset.javaClass.getMethod(
                "setActiveDevice",
                BluetoothDevice::class.java
            )
            setActiveDeviceMethod.invoke(mBluetoothHeadset, device)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set active device: ${e.message}")
        }
    }

    override fun onCallStateChanged(state: Int, incomingNumber: String?) {
        super.onCallStateChanged(state, incomingNumber)
        when (state) {
            TelephonyManager.CALL_STATE_IDLE -> {
                Log.d(TAG, "Call ended")
                ttsManager.speak("Call ended")
            }
            TelephonyManager.CALL_STATE_RINGING -> {
                Log.d(TAG, "Incoming call from $incomingNumber")
                ttsManager.speak("Incoming call from $incomingNumber")
                connectToHeadset()
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                Log.d(TAG, "In a call with $incomingNumber")
                ttsManager.speak("In a call with $incomingNumber")
            }
        }
    }
}

