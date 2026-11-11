package com.priyanshudeveloper.usbnexus.phoenixengine.usb

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.priyanshudeveloper.usbnexus.phoenixengine.data.local.UsbNexusDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DacKeepAliveNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action == ACTION_ENABLE_DAC_KEEP_ALIVE) {
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
            if (notificationId != -1) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(notificationId)
            }

            DacKeepAliveService.startService(context)

            val dataStore = UsbNexusDataStore(context)
            CoroutineScope(Dispatchers.IO).launch {
                dataStore.setDacKeepAliveEnabled(true)
            }
        }
    }

    companion object {
        const val ACTION_ENABLE_DAC_KEEP_ALIVE =
            "com.priyanshudeveloper.usbnexus.ENABLE_DAC_KEEP_ALIVE_NOTIFICATION"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
