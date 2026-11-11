package com.priyanshudeveloper.usbnexus.phoenixengine.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import com.priyanshudeveloper.usbnexus.phoenixengine.data.local.ConnectedDeviceCache
import com.priyanshudeveloper.usbnexus.phoenixengine.data.remote.RemoteConnectedDeviceDataSource
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.ConnectedDevice
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.ConnectionType
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbConfigurationDescriptor
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbEndpointDescriptor
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbInterfaceDescriptor
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbLowLevelDescriptor
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.VerifiedDeviceDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectedDeviceManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager

    private val cache = ConnectedDeviceCache(context)
    private val remoteDataSource = RemoteConnectedDeviceDataSource()

    private val _connectedDevices = MutableStateFlow<List<ConnectedDevice>>(emptyList())
    val connectedDevices: StateFlow<List<ConnectedDevice>> = _connectedDevices.asStateFlow()

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED,
                UsbManager.ACTION_USB_ACCESSORY_ATTACHED,
                UsbManager.ACTION_USB_DEVICE_DETACHED,
                UsbManager.ACTION_USB_ACCESSORY_DETACHED -> {
                    refreshDevices()
                }
            }
        }
    }

    fun startListening() {
        refreshDevices()

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
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(usbReceiver)
        } catch (_: Exception) {
            // Ignored if not registered
        }
    }

    fun refreshDevices() {
        val rawDevices = mutableListOf<ConnectedDevice>()

        // 1. Scan Attached USB Peripherals (Keyboards, Mice, Drives, DACs, PC Hosts)
        usbManager?.deviceList?.values?.forEach { device ->
            val vendorHex = "0x" + device.vendorId.toString(16).padStart(4, '0').uppercase()
            val productHex = "0x" + device.productId.toString(16).padStart(4, '0').uppercase()

            val devName = resolveHumanReadableName(device)
            val desc = resolveUsbClassDescription(device)
            val lowLevel = extractLowLevelDescriptor(device)

            val id = "usb_${device.vendorId}_${device.productId}"
            rawDevices.add(
                ConnectedDevice(
                    id = id,
                    name = devName,
                    connectionType = ConnectionType.USB,
                    primaryDescription = desc,
                    vendorOrMacOrIp = "$vendorHex | $productHex",
                    lowLevelDescriptor = lowLevel
                )
            )
        }

        // 2. Scan Connected USB Accessories (PC Connected via USB Cable in Host / Accessory Mode)
        try {
            usbManager?.accessoryList?.forEach { accessory ->
                val mfg = accessory.manufacturer.takeIf { it.isNotBlank() } ?: "Personal Computer"
                val model = accessory.model.takeIf { it.isNotBlank() } ?: "PC Host Interface"
                val devName = "$mfg ($model)"
                val id = "acc_${mfg.hashCode()}_${model.hashCode()}"

                if (rawDevices.none { it.id == id }) {
                    rawDevices.add(
                        ConnectedDevice(
                            id = id,
                            name = devName,
                            connectionType = ConnectionType.USB,
                            primaryDescription = "PC Host / USB Accessory Interface",
                            vendorOrMacOrIp = "PC Host | USB Accessory Mode"
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // Query Internet & Cache Details for each USB device
        val finalDevices = rawDevices.map { device ->
            val cachedDetails = cache.getCachedDetails(device.id)
            val updatedDevice = if (cachedDetails != null) {
                device.copy(isVerifiedOnline = true, verifiedDetails = cachedDetails)
            } else {
                // Fetch online verified specs in background
                scope.launch {
                    try {
                        val verified = remoteDataSource.queryAndVerifyDeviceOnline(device)
                        cache.cacheDetails(device.id, verified)
                        updateDeviceWithVerifiedDetails(device.id, verified)
                    } catch (_: Exception) {}
                }
                device
            }
            cache.saveDeviceToHistory(updatedDevice)
            updatedDevice
        }

        _connectedDevices.value = finalDevices
    }

    private fun updateDeviceWithVerifiedDetails(deviceId: String, details: VerifiedDeviceDetails) {
        val currentList = _connectedDevices.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == deviceId }
        if (index != -1) {
            val updated = currentList[index].copy(
                isVerifiedOnline = true,
                verifiedDetails = details
            )
            currentList[index] = updated
            _connectedDevices.value = currentList
            cache.saveDeviceToHistory(updated)
        }
    }

    private fun extractLowLevelDescriptor(device: UsbDevice): UsbLowLevelDescriptor {
        val interfaces = mutableListOf<UsbInterfaceDescriptor>()
        val configurations = mutableListOf<UsbConfigurationDescriptor>()

        // Extract Configurations
        for (c in 0 until device.configurationCount) {
            val config = device.getConfiguration(c)
            configurations.add(
                UsbConfigurationDescriptor(
                    id = config.id,
                    name = try { config.name } catch (_: Exception) { null } ?: "Configuration #${config.id}",
                    isSelfPowered = config.isSelfPowered,
                    isRemoteWakeup = config.isRemoteWakeup,
                    maxPowerMa = config.maxPower
                )
            )
        }

        // Extract Interfaces & Endpoints
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            val endpoints = mutableListOf<UsbEndpointDescriptor>()

            for (j in 0 until iface.endpointCount) {
                val ep = iface.getEndpoint(j)
                val dirStr = if (ep.direction == UsbConstants.USB_DIR_IN) "Inbound (0x80)" else "Outbound (0x0)"
                val typeStr = when (ep.type) {
                    UsbConstants.USB_ENDPOINT_XFER_BULK -> "Bulk (0x2)"
                    UsbConstants.USB_ENDPOINT_XFER_INT -> "Interrupt (0x3)"
                    UsbConstants.USB_ENDPOINT_XFER_ISOC -> "Isochronous (0x1)"
                    UsbConstants.USB_ENDPOINT_XFER_CONTROL -> "Control (0x0)"
                    else -> "Transfer Type ${ep.type}"
                }

                endpoints.add(
                    UsbEndpointDescriptor(
                        number = ep.endpointNumber,
                        addressHex = "0x" + ep.address.toString(16).padStart(2, '0').uppercase(),
                        direction = dirStr,
                        type = typeStr,
                        interval = ep.interval,
                        maxPacketSize = ep.maxPacketSize,
                        attributesHex = "0x" + ep.attributes.toString(16).padStart(2, '0').uppercase()
                    )
                )
            }

            interfaces.add(
                UsbInterfaceDescriptor(
                    id = iface.id,
                    name = try { iface.name } catch (_: Exception) { null },
                    alternateSetting = iface.alternateSetting,
                    classDescription = resolveClassCodeText(iface.interfaceClass),
                    subclassDescription = resolveSubclassCodeText(iface.interfaceClass, iface.interfaceSubclass),
                    protocolDescription = resolveProtocolCodeText(iface.interfaceClass, iface.interfaceProtocol),
                    endpoints = endpoints
                )
            )
        }

        val vendorHex = "0x" + device.vendorId.toString(16).padStart(4, '0').uppercase()
        val productHex = "0x" + device.productId.toString(16).padStart(4, '0').uppercase()
        val reportedMfg = try { device.manufacturerName } catch (_: Exception) { null } ?: "SanDisk / Hardware Vendor"
        val reportedProd = try { device.productName } catch (_: Exception) { null } ?: "Ultra Hardware Device"
        val brandDb = resolveVendorBrand(device.vendorId)
        val nameDb = resolveHumanReadableName(device)

        return UsbLowLevelDescriptor(
            devicePath = device.deviceName,
            deviceClassText = resolveClassCodeText(device.deviceClass),
            vendorIdHex = vendorHex,
            vendorNameReported = reportedMfg,
            vendorNameDb = brandDb,
            productIdHex = productHex,
            productNameReported = reportedProd,
            productNameDb = nameDb,
            interfaces = interfaces,
            configurations = configurations
        )
    }

    private fun resolveClassCodeText(classCode: Int): String {
        return when (classCode) {
            0 -> "Use class information in Interface Descriptor (0x0)"
            UsbConstants.USB_CLASS_AUDIO -> "Audio Device (0x1)"
            UsbConstants.USB_CLASS_COMM -> "Communications Device / Modem (0x2)"
            UsbConstants.USB_CLASS_HID -> "Human Interface Device / HID (0x3)"
            UsbConstants.USB_CLASS_PHYSICA -> "Physical Device (0x5)"
            UsbConstants.USB_CLASS_STILL_IMAGE -> "Still Image / Camera Device (0x6)"
            UsbConstants.USB_CLASS_PRINTER -> "Printer Device (0x7)"
            UsbConstants.USB_CLASS_MASS_STORAGE -> "Mass Storage Device (0x8)"
            UsbConstants.USB_CLASS_HUB -> "USB Hub (0x9)"
            UsbConstants.USB_CLASS_CDC_DATA -> "CDC Data Device (0x0A)"
            UsbConstants.USB_CLASS_CSCID -> "Smart Card Device (0x0B)"
            UsbConstants.USB_CLASS_CONTENT_SEC -> "Content Security Device (0x0D)"
            UsbConstants.USB_CLASS_VIDEO -> "Video / UVC Camera Device (0x0E)"
            UsbConstants.USB_CLASS_WIRELESS_CONTROLLER -> "Wireless Controller (0xE0)"
            UsbConstants.USB_CLASS_MISC -> "Miscellaneous Device (0xEF)"
            UsbConstants.USB_CLASS_APP_SPEC -> "Application Specific (0xFE)"
            UsbConstants.USB_CLASS_VENDOR_SPEC -> "Vendor Specific Device (0xFF)"
            else -> "Class Code (0x${classCode.toString(16).uppercase()})"
        }
    }

    private fun resolveSubclassCodeText(classCode: Int, subclassCode: Int): String {
        return when {
            classCode == UsbConstants.USB_CLASS_MASS_STORAGE -> when (subclassCode) {
                1 -> "RBC (Reduced Block Commands) (0x1)"
                2 -> "SFF-8020i / ATAPI CD-ROM (0x2)"
                3 -> "QIC-157 Tape Drive (0x3)"
                4 -> "UFI Floppy / Flash (0x4)"
                5 -> "SFF-8070i Floppy (0x5)"
                6 -> "SCSI Transparent Command Set (0x6)"
                else -> "Subclass (0x${subclassCode.toString(16).uppercase()})"
            }
            classCode == UsbConstants.USB_CLASS_HID -> when (subclassCode) {
                1 -> "Boot Interface Subclass (0x1)"
                else -> "None / Standard Subclass (0x${subclassCode.toString(16).uppercase()})"
            }
            else -> "Subclass (0x${subclassCode.toString(16).uppercase()})"
        }
    }

    private fun resolveProtocolCodeText(classCode: Int, protocolCode: Int): String {
        return when {
            classCode == UsbConstants.USB_CLASS_MASS_STORAGE -> when (protocolCode) {
                80 -> "Bulk-Only Transport / BOT (0x50 / 80)"
                98 -> "UAS (USB Attached SCSI Protocol) (0x62 / 98)"
                else -> "Protocol ($protocolCode)"
            }
            classCode == UsbConstants.USB_CLASS_HID -> when (protocolCode) {
                1 -> "Keyboard Protocol (0x1)"
                2 -> "Mouse / Pointer Protocol (0x2)"
                else -> "Standard HID Protocol ($protocolCode)"
            }
            else -> "Protocol ($protocolCode)"
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
        var isPcInterface = false

        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            when (iface.interfaceClass) {
                UsbConstants.USB_CLASS_HID -> {
                    isHid = true
                    if (iface.interfaceProtocol == 1) isKeyboard = true
                    if (iface.interfaceProtocol == 2) isMouse = true
                }
                UsbConstants.USB_CLASS_MASS_STORAGE -> isMassStorage = true
                UsbConstants.USB_CLASS_COMM, 255 -> isPcInterface = true
            }
        }

        val brand = resolveVendorBrand(device.vendorId)

        return when {
            isKeyboard -> "$brand USB Keyboard"
            isMouse -> "$brand USB Mouse / Optical Pointer"
            isHid -> "$brand USB Human Interface Device (Keyboard/Mouse)"
            isMassStorage -> "$brand USB Storage / Flash Drive"
            isPcInterface || device.vendorId == 0x18D1 || device.vendorId == 0x04E8 || device.vendorId == 0x054C ->
                "$brand Personal Computer (PC Host)"
            else -> "$brand USB Peripheral (0x${device.vendorId.toString(16).uppercase()})"
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
            0x093A -> "PixArt Optical"
            0x04B4 -> "Cypress"
            0x03F0 -> "HP"
            0x413C -> "Dell"
            0x17EF -> "Lenovo"
            else -> "Generic"
        }
    }

    private fun resolveUsbClassDescription(device: UsbDevice): String {
        var hasKeyboard = false
        var hasMouse = false

        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            if (iface.interfaceClass == UsbConstants.USB_CLASS_HID) {
                if (iface.interfaceProtocol == 1) hasKeyboard = true
                if (iface.interfaceProtocol == 2) hasMouse = true
            }
        }

        return when {
            hasKeyboard -> "USB Input Keyboard Peripheral"
            hasMouse -> "USB Optical Mouse / Pointer Device"
            device.deviceClass == UsbConstants.USB_CLASS_HID -> "Human Interface Device (Keyboard/Mouse)"
            device.deviceClass == UsbConstants.USB_CLASS_MASS_STORAGE -> "USB Mass Storage / Flash Drive"
            device.deviceClass == UsbConstants.USB_CLASS_AUDIO -> "USB Audio Device"
            device.deviceClass == UsbConstants.USB_CLASS_COMM -> "USB Communications / Network Adapter"
            device.deviceClass == UsbConstants.USB_CLASS_HUB -> "USB Hub Expansion"
            else -> "USB Hardware Interface"
        }
    }
}
