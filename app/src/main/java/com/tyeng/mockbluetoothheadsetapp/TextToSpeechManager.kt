package com.tyeng.mockbluetoothheadsetapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class BluetoothHeadsetService : Service() {

    companion object {
        private const val TAG = "BluetoothHeadsetService"
    }

    private lateinit var mockBluetoothHeadset: MockBluetoothHeadset
    private lateinit var textToSpeechManager: TextToSpeechManager

    override fun onCreate() {
        super.onCreate()

        mockBluetoothHeadset = MockBluetoothHeadset(this)
        mockBluetoothHeadset.enableBluetooth()

        textToSpeechManager = TextToSpeechManager(this)
        textToSpeechManager.init()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mockBluetoothHeadset.disableBluetooth()
        text
