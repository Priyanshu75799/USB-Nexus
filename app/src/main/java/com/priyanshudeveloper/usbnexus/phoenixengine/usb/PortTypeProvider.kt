package com.priyanshudeveloper.usbnexus.phoenixengine.usb

import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.DeviceProfile
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.repository.DeviceProfileRepository

interface PortTypeProvider {
    suspend fun getDeviceProfile(): DeviceProfile?
    suspend fun saveSelectedPortType(portType: UsbPortType): DeviceProfile
}

class DeviceProfilePortTypeProvider(
    private val repository: DeviceProfileRepository
) : PortTypeProvider {

    override suspend fun getDeviceProfile(): DeviceProfile? {
        return repository.getStoredDeviceProfile()
    }

    override suspend fun saveSelectedPortType(portType: UsbPortType): DeviceProfile {
        return repository.saveSelectedPortType(portType)
    }
}
