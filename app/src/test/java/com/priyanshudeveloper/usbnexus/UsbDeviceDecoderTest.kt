package com.priyanshudeveloper.usbnexus

import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.ConnectedDevice
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.ConnectionType
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsbDeviceDecoderTest {

    @Test
    fun testUsbPortTypeEnumValues() {
        assertEquals("TYPE_C", UsbPortType.TYPE_C.name)
        assertEquals("MICRO_USB", UsbPortType.MICRO_USB.name)
        assertEquals("UNKNOWN", UsbPortType.UNKNOWN.name)
    }

    @Test
    fun testConnectedDeviceModel() {
        val device = ConnectedDevice(
            id = "usb_046d_c52b",
            name = "Logitech USB Receiver",
            connectionType = ConnectionType.USB,
            primaryDescription = "Human Interface Device",
            vendorOrMacOrIp = "0x046D | 0xC52B",
            isVerifiedOnline = true
        )

        assertEquals("usb_046d_c52b", device.id)
        assertEquals("Logitech USB Receiver", device.name)
        assertEquals(ConnectionType.USB, device.connectionType)
        assertTrue(device.isVerifiedOnline)
        assertNotNull(device.vendorOrMacOrIp)
    }

    @Test
    fun testVendorHexFormatting() {
        val vendorId = 0x046D
        val productId = 0xC52B

        val vendorHex = "0x" + vendorId.toString(16).padStart(4, '0').uppercase()
        val productHex = "0x" + productId.toString(16).padStart(4, '0').uppercase()

        assertEquals("0x046D", vendorHex)
        assertEquals("0xC52B", productHex)
    }
}
