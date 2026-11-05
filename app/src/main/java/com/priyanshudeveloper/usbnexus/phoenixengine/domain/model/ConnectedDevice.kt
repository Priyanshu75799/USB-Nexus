package com.priyanshudeveloper.usbnexus.phoenixengine.domain.model

enum class ConnectionType {
    USB
}

data class ConnectedDevice(
    val id: String,
    val name: String,
    val connectionType: ConnectionType = ConnectionType.USB,
    val primaryDescription: String,
    val vendorOrMacOrIp: String,
    val isVerifiedOnline: Boolean = false,
    val verifiedDetails: VerifiedDeviceDetails? = null,
    val lowLevelDescriptor: UsbLowLevelDescriptor? = null
)

data class VerifiedDeviceDetails(
    val deviceName: String,
    val manufacturer: String,
    val modelOrChipset: String,
    val connectionCategory: String,
    val hardwareClass: String,
    val vendorIdOrMac: String,
    val productIdOrPort: String,
    val maxDataRate: String,
    val powerRequirements: String,
    val supportedProtocols: List<String>,
    val securityTrustLevel: String,
    val verificationStatus: String,
    val verificationSource: String,
    val verificationTimestamp: String,
    val extraParameters: Map<String, String>
)

data class UsbEndpointDescriptor(
    val number: Int,
    val addressHex: String,
    val direction: String,
    val type: String,
    val interval: Int,
    val maxPacketSize: Int,
    val attributesHex: String
)

data class UsbInterfaceDescriptor(
    val id: Int,
    val name: String?,
    val alternateSetting: Int,
    val classDescription: String,
    val subclassDescription: String,
    val protocolDescription: String,
    val endpoints: List<UsbEndpointDescriptor>
)

data class UsbConfigurationDescriptor(
    val id: Int,
    val name: String?,
    val isSelfPowered: Boolean,
    val isRemoteWakeup: Boolean,
    val maxPowerMa: Int
)

data class UsbLowLevelDescriptor(
    val devicePath: String,
    val deviceClassText: String,
    val vendorIdHex: String,
    val vendorNameReported: String,
    val vendorNameDb: String,
    val productIdHex: String,
    val productNameReported: String,
    val productNameDb: String,
    val interfaces: List<UsbInterfaceDescriptor>,
    val configurations: List<UsbConfigurationDescriptor>
)
