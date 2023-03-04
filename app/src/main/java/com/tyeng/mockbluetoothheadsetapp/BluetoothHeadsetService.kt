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
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.tyeng.mockbluetoothheadsetapp.com.tyeng.mockbluetoothheadsetapp.TextToSpeechManager

class BluetoothHeadsetService : Service() {

    companion object {
        private const val TAG = "BluetoothHeadsetService"
    }

    private lateinit var audioManager: AudioManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var mockBluetoothHeadset: MockBluetoothHeadset
    private lateinit var mBluetoothHeadset: BluetoothHeadset

    override fun onCreate() {
        super.onCreate()

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        mockBluetoothHeadset = MockBluetoothHeadset(this)
        mockBluetoothHeadset.enableBluetooth()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mockBluetoothHeadset.disableBluetooth()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val mProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            Log.d(TAG, "onServiceConnected")
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = proxy as BluetoothHeadset
                if (ActivityCompat.checkSelfPermission(applicationContext,Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
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
                mBluetoothHeadset.startVoiceRecognition(mBluetoothHeadset.connectedDevices[0])
                Log.d(TAG, "startVoiceRecognition")
                Thread.sleep(3000)
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.BLUETOOTH_CONNECT
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
                mBluetoothHeadset.stopVoiceRecognition(mBluetoothHeadset.connectedDevices[0])
                Log.d(TAG, "stopVoiceRecognition")
                TextToSpeechManager.speak("Incoming call answered.")
                autoAnswer()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            Log.d(TAG, "onServiceDisconnected")
        }
    }

    private fun autoAnswer() {
        try {
            val headset = Class.forName(mBluetoothHeadset.javaClass.name)
            val m = headset.getDeclaredMethod("phoneStateChanged", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java)
            m.isAccessible = true
            m.invoke(mBluetoothHeadset, 1, 0, "")
            Log.d(TAG, "Auto answer.")
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
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
    }
}
