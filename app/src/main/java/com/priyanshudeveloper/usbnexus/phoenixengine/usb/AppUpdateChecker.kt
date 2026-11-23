package com.priyanshudeveloper.usbnexus.phoenixengine.usb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.priyanshudeveloper.usbnexus.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppUpdateChecker {

    private const val CHANNEL_ID = "usb_nexus_app_update"
    private const val NOTIFICATION_ID = 9001
    private const val PREFS_NAME = "usb_nexus_update_prefs"
    private const val KEY_LAST_NOTIFIED_TIME = "last_notified_time"
    private const val INTERVAL_24_HOURS_MS = 24 * 60 * 60 * 1000L

    /**
     * Checks GitHub API for new releases. If a newer version exists and at least 24 hours
     * have elapsed since the last update notification, posts a high-priority system notification.
     */
    suspend fun checkForUpdates(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val currentVersion = try {
                    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    pInfo.versionName ?: "1.7"
                } catch (_: Exception) {
                    "1.7"
                }

                val url = java.net.URL("https://api.github.com/repos/Priyanshu75799/USB-Nexus/releases/latest")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "USBNexus-App")
                connection.connectTimeout = 4000
                connection.readTimeout = 4000

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val tagRegex = """"tag_name"\s*:\s*"([^"]+)"""".toRegex()
                    val match = tagRegex.find(response)
                    val tagName = match?.groupValues?.get(1)?.removePrefix("v")?.trim() ?: ""

                    if (tagName.isNotBlank() && isVersionGreaterThanCurrent(tagName, currentVersion)) {
                        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val lastNotified = prefs.getLong(KEY_LAST_NOTIFIED_TIME, 0L)
                        val now = System.currentTimeMillis()

                        if (now - lastNotified >= INTERVAL_24_HOURS_MS) {
                            sendUpdateNotification(context, tagName)
                            prefs.edit().putLong(KEY_LAST_NOTIFIED_TIME, now).apply()
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore network issues quietly
            }
        }
    }

    private fun sendUpdateNotification(context: Context, newVersion: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Update Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a new version of USB Nexus is released"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val releaseUrl = "https://github.com/Priyanshu75799/USB-Nexus/releases/latest"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_usb_nexus)
            .setContentTitle("USB Nexus Update Available!")
            .setContentText("New version v$newVersion is available on GitHub! Tap to download.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("A new version (v$newVersion) of USB Nexus has been released on GitHub. Tap here to download the latest APK!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            @Suppress("MissingPermission")
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {}
    }

    private fun isVersionGreaterThanCurrent(latest: String, current: String): Boolean {
        try {
            val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (_: Exception) {}
        return false
    }
}
