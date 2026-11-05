package com.priyanshudeveloper.usbnexus.phoenixengine.usb

import android.content.Context
import android.os.Build
import com.priyanshudeveloper.usbnexus.phoenixengine.data.local.PortTypePreferences
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.DeviceProfile
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Clean Local Port Type Detector.
 * Removes all internet model searching logic as requested.
 * Returns the user's stored USB port type selection.
 */
class OnlinePortTypeDetector(private val context: Context? = null) {

    private val preferences = context?.let { PortTypePreferences(it) }

    suspend fun detectDeviceUsbPortProfile(): DeviceProfile = withContext(Dispatchers.IO) {
        val stored = preferences?.getStoredDeviceProfile()
        if (stored != null) {
            return@withContext stored
        }

        val manufacturer = (Build.MANUFACTURER ?: "Android").trim()
        val model = (Build.MODEL ?: "Device").trim()

        DeviceProfile(
            manufacturer = manufacturer,
            model = model,
            marketingName = "$manufacturer $model",
            usbPortType = UsbPortType.UNKNOWN,
            usbHostSupported = true,
            otgSupported = true,
            source = "User Configuration Required",
            confidence = "Pending"
        )
    }
}
