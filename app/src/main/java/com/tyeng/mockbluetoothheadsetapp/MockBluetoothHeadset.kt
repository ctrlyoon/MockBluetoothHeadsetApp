package com.tyeng.mockbluetoothheadsetapp

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import javax.security.auth.callback.Callback


var mBluetoothHeadset: BluetoothHeadset? = null
private var mConnectedBluetoothDevice: BluetoothDevice? = null

private val mBluetoothHeadsetListener: ServiceListener = object : ServiceListener {
    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
        if (profile == BluetoothProfile.HEADSET) {
            mBluetoothHeadset = proxy as BluetoothHeadset
            Log.d(TAG, "BluetoothHeadset proxy obtained")
            val devices = mBluetoothHeadset!!.connectedDevices
            if (!devices.isEmpty()) {
                mConnectedBluetoothDevice = devices[0]
                Log.d(TAG, "Connected Bluetooth Device: " + mConnectedBluetoothDevice!!.name)
            }
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        if (profile == BluetoothProfile.HEADSET) {
            mBluetoothHeadset = null
            mConnectedBluetoothDevice = null
            Log.d(TAG, "BluetoothHeadset proxy lost")
        }
    }
}

private val mBluetoothHeadsetCallback: BluetoothHeadset.Callback = object : Callback() {
    fun onBluetoothStateChanged(state: Int) {
        Log.d(TAG, "BluetoothHeadset state: $state")
    }

    fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
        Log.d(
            TAG,
            "BluetoothHeadset connection state changed: " + state + " for device: " + device.name
        )
        if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
            mConnectedBluetoothDevice = device
            Log.d(TAG, "BluetoothHeadset audio connected: " + mConnectedBluetoothDevice!!.name)
            answerCall()
        } else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
            mConnectedBluetoothDevice = null
            Log.d(TAG, "BluetoothHeadset disconnected")
        }
    }
}

private fun answerCall() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val telecomManager = getSystemService<Any>(Context.TELECOM_SERVICE) as TelecomManager?
        if (telecomManager != null) {
            telecomManager.acceptRingingCall()
            Log.d(TAG, "Call answered automatically")
        }
    } else {
        try {
            // for older API versions
            Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_HEADSETHOOK)
            Log.d(TAG, "Call answered automatically")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
