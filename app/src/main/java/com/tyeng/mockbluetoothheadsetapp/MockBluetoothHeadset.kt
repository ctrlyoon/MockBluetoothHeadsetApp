package com.tyeng.mockbluetoothheadsetapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothHeadsetClient
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

class MockBluetoothHeadset(private val context: Context) {

    companion object {
        private const val TAG = "MockBluetoothHeadset"
    }

    private val btAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        BluetoothAdapter.getDefaultAdapter()
    }

    private val mProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            Log.d(TAG, "onServiceConnected")
            if (profile == BluetoothProfile.HEADSET_CLIENT) {
                val bluetoothHeadsetClient = proxy as BluetoothHeadsetClient
                connectToHeadset(bluetoothHeadsetClient)
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            Log.d(TAG, "onServiceDisconnected")
        }
    }

    fun enableBluetooth() {
        if (btAdapter == null) {
            Log.e(TAG, "Bluetooth is not supported on this device")
            return
        }

        if (!btAdapter!!.isEnabled) {
            Log.d(TAG, "Enabling Bluetooth...")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            btAdapter!!.enable()
        } else {
            Log.d(TAG, "Bluetooth is already enabled")
        }

        connectToHeadset(btAdapter!!.getProfileProxy(context, mProfileListener, BluetoothProfile.HEADSET_CLIENT) as BluetoothHeadsetClient)
    }

    fun disableBluetooth() {
        if (btAdapter == null) {
            Log.e(TAG, "Bluetooth is not supported on this device")
            return
        }

        if (btAdapter!!.isEnabled) {
            Log.d(TAG, "Disabling Bluetooth...")
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            btAdapter!!.disable()
        } else {
            Log.d(TAG, "Bluetooth is already disabled")
        }
    }

    private fun connectToHeadset(bluetoothHeadsetClient: BluetoothHeadsetClient) {
        val pairedDevices: Set<BluetoothDevice>? = btAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            if (bluetoothHeadsetClient.isAudioConnected(device)) {
                bluetoothHeadsetClient.disconnectAudio(device)
                Log.d(TAG, "disconnectAudio")
            } else {
                bluetoothHeadsetClient.connect(device)
                Log.d(TAG, "connect")
            }
        }
    }
}
