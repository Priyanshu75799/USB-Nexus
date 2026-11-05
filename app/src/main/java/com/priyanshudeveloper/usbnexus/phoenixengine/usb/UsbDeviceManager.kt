package com.priyanshudeveloper.usbnexus.phoenixengine.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UsbDeviceInfo(
    val deviceName: String,
    val vendorId: Int,
    val productId: Int,
    val manufacturerName: String?,
    val productName: String?,
    val interfaceCount: Int,
    val deviceClassDescription: String
)

data class NetworkStatusInfo(
    val isEthernetConnected: Boolean = false,
    val isWifiConnected: Boolean = false,
    val isCellularConnected: Boolean = false,
    val activeTransportType: String = "None"
)

class UsbDeviceManager(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _connectedDevices = MutableStateFlow<List<UsbDeviceInfo>>(emptyList())
    val connectedDevices: StateFlow<List<UsbDeviceInfo>> = _connectedDevices.asStateFlow()

    private val _isHostSupported = MutableStateFlow(
        context.packageManager.hasSystemFeature("android.hardware.usb.host")
    )
    val isHostSupported: StateFlow<Boolean> = _isHostSupported.asStateFlow()

    private val _networkStatus = MutableStateFlow(NetworkStatusInfo())
    val networkStatus: StateFlow<NetworkStatusInfo> = _networkStatus.asStateFlow()

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED,
                UsbManager.ACTION_USB_ACCESSORY_ATTACHED,
                UsbManager.ACTION_USB_DEVICE_DETACHED,
                UsbManager.ACTION_USB_ACCESSORY_DETACHED -> {
                    refreshConnectedDevices()
                    checkNetworkStatus()
                }
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            checkNetworkStatus()
        }

        override fun onLost(network: Network) {
            checkNetworkStatus()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            checkNetworkStatus()
        }
    }

    fun startListening() {
        refreshConnectedDevices()
        checkNetworkStatus()

        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
            addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(usbReceiver, filter)
        }

        try {
            val request = NetworkRequest.Builder().build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {
            // Callback registration fallback
        }
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(usbReceiver)
        } catch (_: Exception) {
            // Ignored if not registered
        }
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
            // Ignored if not registered
        }
    }

    fun refreshConnectedDevices() {
        val deviceList = usbManager?.deviceList
        val list = mutableListOf<UsbDeviceInfo>()
        deviceList?.values?.forEach { device ->
            val resolvedName = resolveHumanReadableProductName(device)
            val classDesc = resolveUsbClassDescription(device)
            val mfgName = try { device.manufacturerName } catch (_: Exception) { null } ?: resolveVendorBrand(device.vendorId)

            list.add(
                UsbDeviceInfo(
                    deviceName = device.deviceName,
                    vendorId = device.vendorId,
                    productId = device.productId,
                    manufacturerName = mfgName,
                    productName = resolvedName,
                    interfaceCount = device.interfaceCount,
                    deviceClassDescription = classDesc
                )
            )
        }

        // Check connected USB accessories (PC Host mode)
        try {
            usbManager?.accessoryList?.forEach { accessory ->
                val mfg = accessory.manufacturer.takeIf { it.isNotBlank() } ?: "Personal Computer"
                val model = accessory.model.takeIf { it.isNotBlank() } ?: "PC Host"
                list.add(
                    UsbDeviceInfo(
                        deviceName = "USB Accessory Host",
                        vendorId = 0x18D1,
                        productId = 0x2D00,
                        manufacturerName = mfg,
                        productName = "Personal Computer ($model Host)",
                        interfaceCount = 1,
                        deviceClassDescription = "PC Host / USB Accessory Interface"
                    )
                )
            }
        } catch (_: Exception) {}

        _connectedDevices.value = list
    }

    fun checkNetworkStatus() {
        val cm = connectivityManager ?: return
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)

        val hasEthernet = caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
        val hasWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val hasCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

        val activeType = when {
            hasEthernet -> "Ethernet (LAN / USB Adapter)"
            hasWifi -> "Wi-Fi"
            hasCellular -> "Cellular Mobile Data"
            else -> "Disconnected / Offline"
        }

        _networkStatus.value = NetworkStatusInfo(
            isEthernetConnected = hasEthernet,
            isWifiConnected = hasWifi,
            isCellularConnected = hasCellular,
            activeTransportType = activeType
        )
    }

    private fun resolveHumanReadableProductName(device: UsbDevice): String {
        val prod = try { device.productName } catch (_: Exception) { null }
        if (!prod.isNullOrBlank() && !prod.startsWith("/dev/")) {
            return prod
        }

        var isKeyboard = false
        var isMouse = false
        var isHid = false
        var isStorage = false

        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            if (iface.interfaceClass == UsbConstants.USB_CLASS_HID) {
                isHid = true
                if (iface.interfaceProtocol == 1) isKeyboard = true
                if (iface.interfaceProtocol == 2) isMouse = true
            } else if (iface.interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE) {
                isStorage = true
            }
        }

        val brand = resolveVendorBrand(device.vendorId)

        return when {
            isKeyboard -> "$brand USB Keyboard"
            isMouse -> "$brand USB Mouse / Optical Pointer"
            isHid -> "$brand USB HID Input Device"
            isStorage -> "$brand USB Flash Drive / Storage"
            device.vendorId == 0x18D1 || device.vendorId == 0x04E8 || device.vendorId == 0x054C ->
                "$brand Personal Computer (PC Host)"
            else -> "$brand USB Device (0x${device.productId.toString(16).uppercase()})"
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

    private fun resolveUsbClassDescription(device: UsbDevice): String {
        var isKeyboard = false
        var isMouse = false

        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            if (iface.interfaceClass == UsbConstants.USB_CLASS_HID) {
                if (iface.interfaceProtocol == 1) isKeyboard = true
                if (iface.interfaceProtocol == 2) isMouse = true
            }
        }

        return when {
            isKeyboard -> "USB Input Keyboard Peripheral"
            isMouse -> "USB Optical Mouse / Pointer Device"
            device.deviceClass == UsbConstants.USB_CLASS_HID -> "Human Interface Device (Keyboard/Mouse)"
            device.deviceClass == UsbConstants.USB_CLASS_MASS_STORAGE -> "USB Mass Storage / Flash Drive"
            device.deviceClass == UsbConstants.USB_CLASS_AUDIO -> "USB Audio Device"
            device.deviceClass == UsbConstants.USB_CLASS_COMM -> "USB Communications / Network Adapter"
            else -> "USB Hardware Interface"
        }
    }
}
