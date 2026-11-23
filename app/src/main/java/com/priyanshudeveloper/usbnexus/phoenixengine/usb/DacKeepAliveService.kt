package com.priyanshudeveloper.usbnexus.phoenixengine.usb

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.priyanshudeveloper.usbnexus.phoenixengine.data.local.UsbNexusDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DacKeepAliveService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var monitorJob: Job? = null

    private lateinit var audioManager: AudioManager
    private lateinit var powerManager: PowerManager
    private lateinit var dataStore: UsbNexusDataStore

    private var audioTrack: AudioTrack? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isReceiverRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> checkAndManageKeepAlive()
                Intent.ACTION_SCREEN_ON -> checkAndManageKeepAlive()
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    stopSilentAudioKeepAlive()
                    scope.launch {
                        dataStore.setDacKeepAliveEnabled(false)
                    }
                    stopSelf()
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> checkAndManageKeepAlive()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        dataStore = UsbNexusDataStore(this)

        registerReceivers()
        startMonitoringLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_KEEP_ALIVE -> {
                stopSilentAudioKeepAlive()
                scope.launch {
                    dataStore.setDacKeepAliveEnabled(false)
                }
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_KEEP_ALIVE -> {
                checkAndManageKeepAlive()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
        monitorJob?.cancel()
        stopSilentAudioKeepAlive()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerReceivers() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
                addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            }
            registerReceiver(receiver, filter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceivers() {
        if (isReceiverRegistered) {
            try { unregisterReceiver(receiver) } catch (_: Exception) {}
            isReceiverRegistered = false
        }
    }

    private fun startMonitoringLoop() {
        monitorJob?.cancel()
        monitorJob = scope.launch {
            while (isActive) {
                val isEnabled = dataStore.dacKeepAliveEnabled.first()
                if (!isEnabled) {
                    stopSilentAudioKeepAlive()
                } else {
                    checkAndManageKeepAlive()
                }
                delay(2000)
            }
        }
    }

    private fun checkAndManageKeepAlive() {
        scope.launch {
            val isEnabled = dataStore.dacKeepAliveEnabled.first()
            if (!isEnabled || !isUsbDeviceConnected()) {
                stopSilentAudioKeepAlive()
                return@launch
            }

            val isInteractive = powerManager.isInteractive
            val isMusicPlaying = audioManager.isMusicActive

            // Keep DAC power rail active when screen is OFF or music is paused
            if (!isInteractive || !isMusicPlaying) {
                startSilentAudioKeepAlive()
            } else {
                stopSilentAudioKeepAlive()
            }
        }
    }

    private fun isUsbDeviceConnected(): Boolean {
        // 1. Check attached physical USB devices via UsbManager
        val usbManager = getSystemService(Context.USB_SERVICE) as? UsbManager
        val attachedDevices = usbManager?.deviceList?.values
        if (!attachedDevices.isNullOrEmpty()) {
            return true
        }

        // 2. Check connected audio output devices via AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (devInfo in audioDevices) {
                val type = devInfo.type
                if (type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                    type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                    type == AudioDeviceInfo.TYPE_USB_ACCESSORY) {
                    return true
                }
            }
        }

        return false
    }

    @Synchronized
    private fun startSilentAudioKeepAlive() {
        acquireWakeLock()
        if (audioTrack != null) return

        try {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val bufferSize = maxOf(minBufferSize, 4096)
            val silentBuffer = ByteArray(bufferSize) // All 0s = 0dB Silence

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(attributes)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STATIC
                )
            }

            val writeResult = track.write(silentBuffer, 0, silentBuffer.size)
            if (writeResult >= 0) {
                val frameCount = silentBuffer.size / 4
                track.setLoopPoints(0, frameCount, -1)
                track.play()
            }

            audioTrack = track
        } catch (_: Exception) {
            audioTrack = null
        }
    }

    @Synchronized
    private fun stopSilentAudioKeepAlive() {
        releaseWakeLock()
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (_: Exception) {}
        audioTrack = null
    }

    @Synchronized
    private fun acquireWakeLock() {
        try {
            if (wakeLock == null || wakeLock?.isHeld == false) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "USBNexus:DacKeepAliveWakeLock"
                ).apply {
                    setReferenceCounted(false)
                    acquire(10 * 60 * 1000L) // 10 minutes timeout per refresh
                }
            }
        } catch (_: Exception) {}
    }

    @Synchronized
    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    companion object {
        const val ACTION_START_KEEP_ALIVE = "com.priyanshudeveloper.usbnexus.START_DAC_KEEP_ALIVE"
        const val ACTION_STOP_KEEP_ALIVE = "com.priyanshudeveloper.usbnexus.STOP_DAC_KEEP_ALIVE"

        fun startService(context: Context) {
            val intent = Intent(context, DacKeepAliveService::class.java).apply {
                action = ACTION_START_KEEP_ALIVE
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }

        fun stopService(context: Context) {
            val intent = Intent(context, DacKeepAliveService::class.java).apply {
                action = ACTION_STOP_KEEP_ALIVE
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }
}
