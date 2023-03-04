import android.app.Service
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.view.KeyEvent
import android.widget.Toast

class BluetoothHeadsetService : Service() {

    private var mBinder: IBinder = LocalBinder()
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mBluetoothHeadset: BluetoothHeadset
    private lateinit var mBluetoothProfile: BluetoothProfile
    private lateinit var mHandler: Handler

    private val mHeadsetProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = proxy as BluetoothHeadset
                mBluetoothProfile = proxy
                mHandler.post { Toast.makeText(applicationContext, "Headset profile connected", Toast.LENGTH_SHORT).show() }
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null
                mBluetoothProfile = null
                mHandler.post { Toast.makeText(applicationContext, "Headset profile disconnected", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        mHandler = Handler()

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothAdapter.getProfileProxy(this, mHeadsetProfileListener, BluetoothProfile.HEADSET)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()

        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothProfile)
    }

    inner class LocalBinder : Binder() {
        val service: BluetoothHeadsetService
            get() = this@BluetoothHeadsetService
    }

    fun answerCall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mBluetoothHeadset != null) {
                mBluetoothHeadset.javaClass.getMethod("acceptCall").invoke(mBluetoothHeadset)
                mHandler.postDelayed({
                    mBluetoothHeadset.javaClass.getMethod("endCall").invoke(mBluetoothHeadset)
                }, 3000)
            }
        } else {
            val mHeadset = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)
            val mAudio = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP)
            if (mHeadset == BluetoothHeadset.STATE_CONNECTED || mAudio == BluetoothA2dp.STATE_CONNECTED) {
                val mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val btnState = mAudioManager.getMode()
                mAudioManager.setMode(AudioManager.MODE_IN_CALL)
                val mKeyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK)
                try {
                    val dispatchMethod = AudioManager::class.java.getMethod("dispatchMediaKeyEvent", KeyEvent::class.java)
                    dispatchMethod.invoke(mAudioManager, mKeyEvent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mAudioManager.setMode(btnState)
            }
        }
    }
}
