package com.priyanshudeveloper.usbnexus.phoenixengine.usb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.priyanshudeveloper.usbnexus.MainActivity
import com.priyanshudeveloper.usbnexus.R

class UsbSecurityNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "USB Connection Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant background alerts when USB hardware is attached"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun sendDeviceConnectedNotification(deviceName: String) {
        val now = System.currentTimeMillis()
        val lastTime = lastNotificationTimes[deviceName] ?: 0L
        if (now - lastTime < DEBOUNCE_INTERVAL_MS) {
            return
        }
        lastNotificationTimes[deviceName] = now

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val uniqueId = NOTIFICATION_BASE_ID + (deviceName.hashCode().let { if (it < 0) -it else it } % 5000)

        val enableDacIntent = Intent(context, DacKeepAliveNotificationReceiver::class.java).apply {
            action = DacKeepAliveNotificationReceiver.ACTION_ENABLE_DAC_KEEP_ALIVE
            putExtra(DacKeepAliveNotificationReceiver.EXTRA_NOTIFICATION_ID, uniqueId)
        }
        val enableDacPendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueId,
            enableDacIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_usb_nexus)
            .setContentTitle("🔌 USB Hardware Connected")
            .setContentText("$deviceName connected & verified.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Connected Device: '$deviceName'. Tap to view specifications or enable DAC Anti-Suspend.")
            )
            .addAction(
                android.R.drawable.ic_media_play,
                "Enable DAC Anti-Suspend",
                enableDacPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            @Suppress("MissingPermission")
            notificationManager?.notify(uniqueId, notification)
        } catch (_: Exception) {}
    }

    companion object {
        private const val CHANNEL_ID = "usb_nexus_device_notifications"
        private const val NOTIFICATION_BASE_ID = 2000
        private const val DEBOUNCE_INTERVAL_MS = 2500L
        private val lastNotificationTimes = java.util.concurrent.ConcurrentHashMap<String, Long>()
    }
}
