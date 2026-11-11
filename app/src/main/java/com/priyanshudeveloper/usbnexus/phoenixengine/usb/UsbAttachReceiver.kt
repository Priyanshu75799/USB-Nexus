package com.priyanshudeveloper.usbnexus.phoenixengine.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build

class UsbAttachReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED ||
            intent.action == UsbManager.ACTION_USB_ACCESSORY_ATTACHED) {

            val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            }

            val notificationManager = UsbSecurityNotificationManager(context)

            if (device != null) {
                val devName = resolveHumanReadableName(device)
                notificationManager.sendDeviceConnectedNotification(devName)
            } else {
                val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
                val attachedDevices = usbManager?.deviceList?.values
                if (!attachedDevices.isNullOrEmpty()) {
                    attachedDevices.forEach { dev ->
                        val devName = resolveHumanReadableName(dev)
                        notificationManager.sendDeviceConnectedNotification(devName)
                    }
                }
            }
        }
    }

    private fun resolveHumanReadableName(device: UsbDevice): String {
        val rawProd = try { device.productName } catch (_: Exception) { null }
        val rawMfg = try { device.manufacturerName } catch (_: Exception) { null }

        if (!rawProd.isNullOrBlank() && !rawProd.startsWith("/dev/") && rawProd != "USB Peripheral") {
            return if (!rawMfg.isNullOrBlank() && !rawProd.contains(rawMfg, ignoreCase = true)) {
                "$rawMfg $rawProd"
            } else {
                rawProd
            }
        }

        var isKeyboard = false
        var isMouse = false
        var isHid = false
        var isMassStorage = false

        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            when (iface.interfaceClass) {
                UsbConstants.USB_CLASS_HID -> {
                    isHid = true
                    if (iface.interfaceProtocol == 1) isKeyboard = true
                    if (iface.interfaceProtocol == 2) isMouse = true
                }
                UsbConstants.USB_CLASS_MASS_STORAGE -> isMassStorage = true
            }
        }

        val brand = resolveVendorBrand(device.vendorId)

        return when {
            isKeyboard -> "$brand USB Keyboard"
            isMouse -> "$brand USB Mouse"
            isHid -> "$brand USB Human Interface Device"
            isMassStorage -> "$brand USB Storage / Flash Drive"
            device.vendorId == 0x18D1 || device.vendorId == 0x04E8 || device.vendorId == 0x054C ->
                "$brand Personal Computer (PC Host)"
            else -> "$brand USB Device"
        }
    }

    private fun resolveVendorBrand(vendorId: Int): String {
        return when (vendorId) {
            0x046D -> "Logitech"
            0x045E -> "Microsoft"
            0x0781 -> "SanDisk"
            0x0BDA -> "Realtek"
            0x0930 -> "Toshiba"
            0x054C -> "Sony"
            0x04E8 -> "Samsung"
            0x18D1 -> "Google PC"
            0x04F2 -> "Chicony"
            0x0E6F -> "PDP"
            0x1C4F -> "SiGma Micro"
            0x093A -> "PixArt"
            0x03F0 -> "HP"
            0x413C -> "Dell"
            0x17EF -> "Lenovo"
            else -> "Generic"
        }
    }
}
