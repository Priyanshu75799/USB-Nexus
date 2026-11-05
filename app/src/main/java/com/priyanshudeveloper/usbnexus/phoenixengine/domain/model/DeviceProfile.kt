package com.priyanshudeveloper.usbnexus.phoenixengine.domain.model

data class DeviceProfile(
    val manufacturer: String,
    val model: String,
    val marketingName: String?,
    val usbPortType: UsbPortType,
    val usbHostSupported: Boolean?,
    val otgSupported: Boolean?,
    val source: String?,
    val confidence: String?
)
