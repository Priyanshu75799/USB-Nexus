package com.priyanshudeveloper.usbnexus.phoenixengine.data.local

import android.content.Context
import android.os.Build
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.DeviceProfile
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType

class LocalDeviceProfileDataSource {

    fun getProfile(
        manufacturer: String = Build.MANUFACTURER ?: "Android",
        model: String = Build.MODEL ?: "Device",
        device: String = Build.DEVICE ?: "",
        product: String = Build.PRODUCT ?: "",
        context: Context? = null
    ): DeviceProfile {
        val mfRaw = manufacturer.ifBlank { Build.MANUFACTURER ?: "Android" }.trim()
        val modelRaw = model.ifBlank { Build.MODEL ?: "Device" }.trim()

        return DeviceProfile(
            manufacturer = mfRaw,
            model = modelRaw,
            marketingName = "$mfRaw $modelRaw",
            usbPortType = UsbPortType.UNKNOWN,
            usbHostSupported = true,
            otgSupported = true,
            source = "User Configuration Required",
            confidence = "Pending"
        )
    }
}
