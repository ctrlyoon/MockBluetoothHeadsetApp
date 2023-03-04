package com.tyeng.mockbluetoothheadsetapp

import android.Manifest.permission.ANSWER_PHONE_CALLS
import android.content.Context
import android.os.Build
import android.os.Handler
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class MockBluetoothHeadset(private val context: Context) {

    private var mService: BluetoothHeadsetService? = null
    private var mCallbacks: BluetoothHeadsetServiceCallbacks? = null

    private val mHandler = Handler()

    private var mIsSimulatingHeadset = false

    fun start() {
        mService = BluetoothHeadsetService(context, object : BluetoothHeadsetServiceCallbacks {
            override fun onHeadsetConnected() {
                Log.d(TAG, "Headset connected")
                mIsSimulatingHeadset = true
            }

            override fun onHeadsetDisconnected() {
                Log.d(TAG, "Headset disconnected")
                mIsSimulatingHeadset = false
            }

            override fun onScoAudioConnected() {
                Log.d(TAG, "SCO audio connected")
                mHandler.postDelayed({
                    acceptCall()
                }, 3000) // Wait 3 seconds before accepting the call
            }

            override fun onScoAudioDisconnected() {
                Log.d(TAG, "SCO audio disconnected")
            }

            override fun onError() {
                Log.d(TAG, "Error occurred")
            }
        })

        mService?.start()
    }

    fun stop() {
        mService?.stop()
        mService = null
    }

    private fun acceptCall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            answerRingingCall()
        } else {
            answerCall()
        }
    }

    @Suppress("DEPRECATION")
    private fun answerCall() {
        try {
            val telephonyService = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val m = telephonyService.javaClass.getDeclaredMethod("answerRingingCall")
            m.invoke(telephonyService)
        } catch (e: Exception) {
            Log.e(TAG, "Error answering call: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun answerRingingCall() {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
            == PackageManager.PERMISSION_GRANTED) {
            telecomManager.acceptRingingCall()
        } else {
            Log.e(TAG, "Permission to answer phone calls not granted")
        }
    }

    companion object {
        private const val TAG = "MockBluetoothHeadset"
    }
}
