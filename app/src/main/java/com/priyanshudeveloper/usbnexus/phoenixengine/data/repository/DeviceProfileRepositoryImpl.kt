package com.priyanshudeveloper.usbnexus.phoenixengine.data.repository

import android.content.Context
import android.os.Build
import com.priyanshudeveloper.usbnexus.phoenixengine.data.local.PortTypePreferences
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.DeviceProfile
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.repository.DeviceProfileRepository

class DeviceProfileRepositoryImpl(
    context: Context? = null
) : DeviceProfileRepository {

    private val preferences = context?.let { PortTypePreferences(it) }

    override suspend fun getStoredDeviceProfile(): DeviceProfile? {
        return preferences?.getStoredDeviceProfile()
    }

    override suspend fun saveSelectedPortType(portType: UsbPortType): DeviceProfile {
        val manufacturer = Build.MANUFACTURER ?: "Android"
        val model = Build.MODEL ?: "Device"
        val profile = DeviceProfile(
            manufacturer = manufacturer,
            model = model,
            marketingName = "$manufacturer $model",
            usbPortType = portType,
            usbHostSupported = true,
            otgSupported = true,
            source = "User Selection",
            confidence = "User Verified"
        )
        preferences?.saveDeviceProfile(profile)
        return profile
    }

    override suspend fun getDeviceProfile(
        manufacturer: String,
        model: String,
        device: String,
        product: String
    ): DeviceProfile? {
        return getStoredDeviceProfile()
    }
}
