package com.priyanshudeveloper.usbnexus.phoenixengine.data.remote

import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.DeviceProfile

/**
 * Clean Remote Device Profile Data Source.
 * Internet model search has been completely removed as requested.
 */
interface RemoteDeviceProfileDataSource {
    suspend fun fetchDeviceProfile(
        manufacturer: String,
        model: String,
        device: String = "",
        product: String = ""
    ): DeviceProfile?
}

class RemoteDeviceProfileDataSourceImpl : RemoteDeviceProfileDataSource {
    override suspend fun fetchDeviceProfile(
        manufacturer: String,
        model: String,
        device: String,
        product: String
    ): DeviceProfile? {
        return null
    }
}
