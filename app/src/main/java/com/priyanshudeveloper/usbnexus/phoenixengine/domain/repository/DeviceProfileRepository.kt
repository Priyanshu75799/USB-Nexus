package com.priyanshudeveloper.usbnexus.phoenixengine.domain.repository

import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.DeviceProfile
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType

interface DeviceProfileRepository {
    suspend fun getStoredDeviceProfile(): DeviceProfile?
    suspend fun saveSelectedPortType(portType: UsbPortType): DeviceProfile
    suspend fun getDeviceProfile(
        manufacturer: String = "",
        model: String = "",
        device: String = "",
        product: String = ""
    ): DeviceProfile?
}
