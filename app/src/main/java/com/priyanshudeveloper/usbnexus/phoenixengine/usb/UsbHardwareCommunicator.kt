package com.priyanshudeveloper.usbnexus.phoenixengine.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UsbTransferResult(
    val success: Boolean,
    val bytesTransferred: Int,
    val dataHex: String,
    val errorMessage: String? = null
)

class UsbHardwareCommunicator(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager

    val permissionStatusFlow: StateFlow<Map<String, Boolean>> = _permissionStatusFlow.asStateFlow()

    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_USB_PERMISSION) {
                synchronized(this) {
                    val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    val isGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    device?.let { dev ->
                        val currentMap = _permissionStatusFlow.value.toMutableMap()
                        currentMap[dev.deviceName] = isGranted
                        _permissionStatusFlow.value = currentMap
                    }
                }
            }
        }
    }

    fun startListeningForPermissions() {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        ContextCompat.registerReceiver(
            context,
            usbPermissionReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    fun stopListeningForPermissions() {
        try {
            context.unregisterReceiver(usbPermissionReceiver)
        } catch (_: Exception) {}
    }

    fun hasPermission(device: UsbDevice): Boolean {
        val systemGranted = usbManager?.hasPermission(device) == true
        if (systemGranted) {
            val currentMap = _permissionStatusFlow.value.toMutableMap()
            if (currentMap[device.deviceName] != true) {
                currentMap[device.deviceName] = true
                _permissionStatusFlow.value = currentMap
            }
        }
        return systemGranted || _permissionStatusFlow.value[device.deviceName] == true
    }

    fun requestPermission(device: UsbDevice) {
        val manager = usbManager ?: return
        if (hasPermission(device)) {
            val currentMap = _permissionStatusFlow.value.toMutableMap()
            currentMap[device.deviceName] = true
            _permissionStatusFlow.value = currentMap
            return
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION).apply {
                setPackage(context.packageName)
            },
            flags
        )

        manager.requestPermission(device, permissionIntent)
    }

    fun openConnection(device: UsbDevice): UsbDeviceConnection? {
        val manager = usbManager ?: return null
        if (!hasPermission(device)) return null
        return manager.openDevice(device)
    }

    fun readDeviceRawDescriptor(device: UsbDevice): String {
        val connection = openConnection(device) ?: return "Permission Denied or Hardware Connection Error"
        val rawBuffer = ByteArray(255)
        return try {
            val length = connection.controlTransfer(
                UsbConstants.USB_DIR_IN,
                6, // GET_DESCRIPTOR
                0x0100, // DEVICE Descriptor
                0,
                rawBuffer,
                rawBuffer.size,
                2000
            )
            connection.close()
            if (length > 0) {
                rawBuffer.copyOf(length).joinToString(" ") { "%02X".format(it) }
            } else {
                "Unable to read raw USB descriptor from hardware connection."
            }
        } catch (e: Exception) {
            connection.close()
            "Error reading hardware descriptor: ${e.message}"
        }
    }

    fun performBulkTransfer(
        device: UsbDevice,
        usbInterface: UsbInterface,
        endpoint: UsbEndpoint,
        data: ByteArray,
        timeoutMs: Int = 2000
    ): UsbTransferResult {
        val connection = openConnection(device)
            ?: return UsbTransferResult(false, 0, "", "No USB permission or failed to open hardware connection")

        if (!connection.claimInterface(usbInterface, true)) {
            connection.close()
            return UsbTransferResult(false, 0, "", "Failed to claim USB interface #${usbInterface.id}")
        }

        return try {
            val bytesTransferred = connection.bulkTransfer(endpoint, data, data.size, timeoutMs)
            connection.releaseInterface(usbInterface)
            connection.close()

            if (bytesTransferred >= 0) {
                val hexStr = data.copyOf(bytesTransferred).joinToString(" ") { "%02X".format(it) }
                UsbTransferResult(true, bytesTransferred, hexStr)
            } else {
                UsbTransferResult(false, 0, "", "Bulk transfer timed out or failed (code: $bytesTransferred)")
            }
        } catch (e: Exception) {
            connection.releaseInterface(usbInterface)
            connection.close()
            UsbTransferResult(false, 0, "", "Hardware transfer error: ${e.message}")
        }
    }

    fun findUsbDeviceByName(deviceName: String): UsbDevice? {
        return usbManager?.deviceList?.values?.find { it.deviceName == deviceName }
    }

    fun findUsbDevice(
        devicePath: String? = null,
        deviceName: String? = null,
        vendorOrMacOrIp: String? = null
    ): UsbDevice? {
        val list = usbManager?.deviceList?.values ?: return null
        if (!devicePath.isNullOrBlank()) {
            list.find { it.deviceName == devicePath }?.let { return it }
        }
        if (!deviceName.isNullOrBlank()) {
            list.find { it.deviceName == deviceName }?.let { return it }
        }
        if (!vendorOrMacOrIp.isNullOrBlank()) {
            list.find { dev ->
                val vHex = "0x" + dev.vendorId.toString(16).padStart(4, '0').uppercase()
                val pHex = "0x" + dev.productId.toString(16).padStart(4, '0').uppercase()
                "$vHex | $pHex" == vendorOrMacOrIp
            }?.let { return it }
        }
        return list.firstOrNull()
    }

    fun handleUsbIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (action == UsbManager.ACTION_USB_DEVICE_ATTACHED || action == ACTION_USB_PERMISSION) {
            val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            }
            device?.let { dev ->
                val isGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true)
                val systemHas = hasPermission(dev)
                if (isGranted || systemHas) {
                    val currentMap = _permissionStatusFlow.value.toMutableMap()
                    currentMap[dev.deviceName] = true
                    _permissionStatusFlow.value = currentMap
                }
            }
        }
    }

    companion object {
        const val ACTION_USB_PERMISSION = "com.priyanshudeveloper.usbnexus.USB_PERMISSION"
        private val _permissionStatusFlow = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    }
}
