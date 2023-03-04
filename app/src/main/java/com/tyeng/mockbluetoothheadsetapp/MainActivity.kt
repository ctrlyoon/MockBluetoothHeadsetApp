import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class MainActivity : Activity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var audioManager: AudioManager
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.US
            }
        }

        // Enable Bluetooth
        if (!btAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableIntent)
        }

        // Start mock Bluetooth headset service
        val startIntent = Intent(this, BluetoothHeadsetService::class.java)
        startService(startIntent)

        // Automatically answer incoming call after 3 seconds
        Thread.sleep(3000)
        if (audioManager.mode == AudioManager.MODE_IN_CALL) {
            Log.d(TAG, "Answering incoming call...")
            audioManager.isSpeakerphoneOn = true
            audioManager.setBluetoothScoOn(true)
            audioManager.startBluetoothSco()
            audioManager.setMode(AudioManager.MODE_IN_CALL)
            tts.speak("Incoming call answered.", TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
