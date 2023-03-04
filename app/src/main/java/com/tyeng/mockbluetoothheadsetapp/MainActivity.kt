package com.tyeng.mockbluetoothheadsetapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var btAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        val intent = Intent(this, BluetoothHeadsetService::class.java)
        startService(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        if (!btAdapter.isEnabled) {
            btAdapter.enable()
        }
        btAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.HEADSET)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        btAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset)
    }

    private val mProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = proxy as BluetoothProfile
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null
            }
        }
    }

    private val mBluetoothHeadset = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                Log.d(TAG, "Bluetooth headset connected")
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                Log.d(TAG, "Bluetooth headset disconnected")
            }
        }
    }
}
