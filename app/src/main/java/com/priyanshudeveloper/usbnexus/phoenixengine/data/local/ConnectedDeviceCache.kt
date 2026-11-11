package com.priyanshudeveloper.usbnexus.phoenixengine.data.local

import android.content.Context
import android.content.SharedPreferences
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.ConnectedDevice
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.ConnectionType
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbConfigurationDescriptor
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbEndpointDescriptor
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbInterfaceDescriptor
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbLowLevelDescriptor
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.VerifiedDeviceDetails
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConnectedDeviceCache(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("usb_nexus_connected_devices_cache", Context.MODE_PRIVATE)

    fun getCachedDetails(deviceId: String): VerifiedDeviceDetails? {
        val jsonStr = prefs.getString("device_$deviceId", null) ?: return null
        return deserializeVerifiedDetails(jsonStr)
    }

    fun cacheDetails(deviceId: String, details: VerifiedDeviceDetails) {
        try {
            val json = serializeVerifiedDetails(details)
            prefs.edit().putString("device_$deviceId", json.toString()).apply()
        } catch (_: Exception) {}
    }

    fun saveDeviceToHistory(
        device: ConnectedDevice
    ) {
        try {
            val jsonStr = prefs.getString("usb_device_history_list", null)
            val historyArray = if (!jsonStr.isNullOrBlank()) JSONArray(jsonStr) else JSONArray()
            val timeNow = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val entryObj = JSONObject().apply {
                put("id", device.id)
                put("name", device.name)
                put("connectionType", device.connectionType.name)
                put("description", device.primaryDescription)
                put("vendorOrMac", device.vendorOrMacOrIp)
                put("timestamp", timeNow)
                device.verifiedDetails?.let { put("details", serializeVerifiedDetails(it)) }
                device.lowLevelDescriptor?.let { put("lowLevel", serializeLowLevel(it)) }
            }

            val updatedArray = JSONArray()
            updatedArray.put(entryObj)
            for (i in 0 until historyArray.length()) {
                val existing = historyArray.getJSONObject(i)
                if (existing.optString("id") != device.id) {
                    updatedArray.put(existing)
                }
            }

            prefs.edit().putString("usb_device_history_list", updatedArray.toString()).apply()
        } catch (_: Exception) {}
    }

    fun removeDeviceFromHistory(deviceId: String) {
        try {
            val jsonStr = prefs.getString("usb_device_history_list", null) ?: return
            val historyArray = JSONArray(jsonStr)
            val updatedArray = JSONArray()
            for (i in 0 until historyArray.length()) {
                val existing = historyArray.getJSONObject(i)
                if (existing.optString("id") != deviceId) {
                    updatedArray.put(existing)
                }
            }
            prefs.edit().putString("usb_device_history_list", updatedArray.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getSavedUsbDevices(): List<ConnectedDevice> {
        val list = mutableListOf<ConnectedDevice>()
        try {
            val jsonStr = prefs.getString("usb_device_history_list", null) ?: return list
            val historyArray = JSONArray(jsonStr)
            for (i in 0 until historyArray.length()) {
                val obj = historyArray.getJSONObject(i)
                val id = obj.optString("id", "usb_$i")
                val name = obj.optString("name", "USB Device")
                val desc = obj.optString("description", "USB Hardware")
                val vendorOrMac = obj.optString("vendorOrMac", "Unknown")

                val detailsObj = obj.optJSONObject("details")
                val verifiedDetails = if (detailsObj != null) deserializeVerifiedDetails(detailsObj.toString()) else null

                val lowLevelObj = obj.optJSONObject("lowLevel")
                val lowLevel = if (lowLevelObj != null) deserializeLowLevel(lowLevelObj) else null

                list.add(
                    ConnectedDevice(
                        id = id,
                        name = name,
                        connectionType = ConnectionType.USB,
                        primaryDescription = desc,
                        vendorOrMacOrIp = vendorOrMac,
                        isVerifiedOnline = verifiedDetails != null,
                        verifiedDetails = verifiedDetails,
                        lowLevelDescriptor = lowLevel
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun getDeviceHistoryList(): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        try {
            val jsonStr = prefs.getString("usb_device_history_list", null) ?: return list
            val historyArray = JSONArray(jsonStr)
            for (i in 0 until historyArray.length()) {
                list.add(historyArray.getJSONObject(i))
            }
        } catch (_: Exception) {}
        return list
    }

    private fun serializeVerifiedDetails(details: VerifiedDeviceDetails): JSONObject {
        return JSONObject().apply {
            put("deviceName", details.deviceName)
            put("manufacturer", details.manufacturer)
            put("modelOrChipset", details.modelOrChipset)
            put("connectionCategory", details.connectionCategory)
            put("hardwareClass", details.hardwareClass)
            put("vendorIdOrMac", details.vendorIdOrMac)
            put("productIdOrPort", details.productIdOrPort)
            put("maxDataRate", details.maxDataRate)
            put("powerRequirements", details.powerRequirements)
            put("protocols", JSONArray(details.supportedProtocols))
            put("securityTrustLevel", details.securityTrustLevel)
            put("verificationStatus", details.verificationStatus)
            put("verificationSource", details.verificationSource)
            put("verificationTimestamp", details.verificationTimestamp)

            val extrasObj = JSONObject()
            details.extraParameters.forEach { (k, v) -> extrasObj.put(k, v) }
            put("extras", extrasObj)
        }
    }

    private fun deserializeVerifiedDetails(jsonStr: String): VerifiedDeviceDetails? {
        return try {
            val json = JSONObject(jsonStr)
            val protocolsArray = json.optJSONArray("protocols") ?: JSONArray()
            val protocols = mutableListOf<String>()
            for (i in 0 until protocolsArray.length()) {
                protocols.add(protocolsArray.getString(i))
            }

            val extrasObj = json.optJSONObject("extras") ?: JSONObject()
            val extras = mutableMapOf<String, String>()
            val keys = extrasObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                extras[key] = extrasObj.getString(key)
            }

            VerifiedDeviceDetails(
                deviceName = json.optString("deviceName", "Device"),
                manufacturer = json.optString("manufacturer", "Verified Hardware Provider"),
                modelOrChipset = json.optString("modelOrChipset", "Standard Peripheral"),
                connectionCategory = json.optString("connectionCategory", "Hardware Interface"),
                hardwareClass = json.optString("hardwareClass", "Peripheral"),
                vendorIdOrMac = json.optString("vendorIdOrMac", "Unknown"),
                productIdOrPort = json.optString("productIdOrPort", "N/A"),
                maxDataRate = json.optString("maxDataRate", "High Speed"),
                powerRequirements = json.optString("powerRequirements", "Standard Bus Power"),
                supportedProtocols = protocols,
                securityTrustLevel = json.optString("securityTrustLevel", "Verified Trust Level 1"),
                verificationStatus = json.optString("verificationStatus", "VERIFIED ONLINE ✓"),
                verificationSource = json.optString("verificationSource", "USB Nexus Verified Database"),
                verificationTimestamp = json.optString("verificationTimestamp", "Cached"),
                extraParameters = extras
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun serializeLowLevel(lowLevel: UsbLowLevelDescriptor): JSONObject {
        val interfacesArray = JSONArray()
        lowLevel.interfaces.forEach { iface ->
            val endpointsArray = JSONArray()
            iface.endpoints.forEach { ep ->
                val epObj = JSONObject().apply {
                    put("number", ep.number)
                    put("addressHex", ep.addressHex)
                    put("direction", ep.direction)
                    put("type", ep.type)
                    put("interval", ep.interval)
                    put("maxPacketSize", ep.maxPacketSize)
                    put("attributesHex", ep.attributesHex)
                }
                endpointsArray.put(epObj)
            }

            val ifaceObj = JSONObject().apply {
                put("id", iface.id)
                put("name", iface.name ?: JSONObject.NULL)
                put("alternateSetting", iface.alternateSetting)
                put("classDescription", iface.classDescription)
                put("subclassDescription", iface.subclassDescription)
                put("protocolDescription", iface.protocolDescription)
                put("endpoints", endpointsArray)
            }
            interfacesArray.put(ifaceObj)
        }

        val configsArray = JSONArray()
        lowLevel.configurations.forEach { cfg ->
            val cfgObj = JSONObject().apply {
                put("id", cfg.id)
                put("name", cfg.name ?: JSONObject.NULL)
                put("isSelfPowered", cfg.isSelfPowered)
                put("isRemoteWakeup", cfg.isRemoteWakeup)
                put("maxPowerMa", cfg.maxPowerMa)
            }
            configsArray.put(cfgObj)
        }

        return JSONObject().apply {
            put("devicePath", lowLevel.devicePath)
            put("deviceClassText", lowLevel.deviceClassText)
            put("vendorIdHex", lowLevel.vendorIdHex)
            put("vendorNameReported", lowLevel.vendorNameReported)
            put("vendorNameDb", lowLevel.vendorNameDb)
            put("productIdHex", lowLevel.productIdHex)
            put("productNameReported", lowLevel.productNameReported)
            put("productNameDb", lowLevel.productNameDb)
            put("interfaces", interfacesArray)
            put("configurations", configsArray)
        }
    }

    private fun deserializeLowLevel(json: JSONObject): UsbLowLevelDescriptor {
        val interfacesList = mutableListOf<UsbInterfaceDescriptor>()
        val interfacesArray = json.optJSONArray("interfaces")
        if (interfacesArray != null) {
            for (i in 0 until interfacesArray.length()) {
                val ifaceObj = interfacesArray.optJSONObject(i) ?: continue
                val endpointsList = mutableListOf<UsbEndpointDescriptor>()
                val endpointsArray = ifaceObj.optJSONArray("endpoints")
                if (endpointsArray != null) {
                    for (j in 0 until endpointsArray.length()) {
                        val epObj = endpointsArray.optJSONObject(j) ?: continue
                        endpointsList.add(
                            UsbEndpointDescriptor(
                                number = epObj.optInt("number", 0),
                                addressHex = epObj.optString("addressHex", "0x00"),
                                direction = epObj.optString("direction", "Unknown"),
                                type = epObj.optString("type", "Bulk"),
                                interval = epObj.optInt("interval", 0),
                                maxPacketSize = epObj.optInt("maxPacketSize", 0),
                                attributesHex = epObj.optString("attributesHex", "0x00")
                            )
                        )
                    }
                }

                val ifaceName = if (ifaceObj.isNull("name")) null else ifaceObj.optString("name")

                interfacesList.add(
                    UsbInterfaceDescriptor(
                        id = ifaceObj.optInt("id", 0),
                        name = ifaceName,
                        alternateSetting = ifaceObj.optInt("alternateSetting", 0),
                        classDescription = ifaceObj.optString("classDescription", "Unknown Class"),
                        subclassDescription = ifaceObj.optString("subclassDescription", "Unknown Subclass"),
                        protocolDescription = ifaceObj.optString("protocolDescription", "Unknown Protocol"),
                        endpoints = endpointsList
                    )
                )
            }
        }

        val configsList = mutableListOf<UsbConfigurationDescriptor>()
        val configsArray = json.optJSONArray("configurations")
        if (configsArray != null) {
            for (i in 0 until configsArray.length()) {
                val cfgObj = configsArray.optJSONObject(i) ?: continue
                val cfgName = if (cfgObj.isNull("name")) null else cfgObj.optString("name")
                configsList.add(
                    UsbConfigurationDescriptor(
                        id = cfgObj.optInt("id", 1),
                        name = cfgName,
                        isSelfPowered = cfgObj.optBoolean("isSelfPowered", false),
                        isRemoteWakeup = cfgObj.optBoolean("isRemoteWakeup", false),
                        maxPowerMa = cfgObj.optInt("maxPowerMa", 500)
                    )
                )
            }
        }

        return UsbLowLevelDescriptor(
            devicePath = json.optString("devicePath", "/dev/bus/usb/001"),
            deviceClassText = json.optString("deviceClassText", "Interface Class"),
            vendorIdHex = json.optString("vendorIdHex", "0x0000"),
            vendorNameReported = json.optString("vendorNameReported", "Vendor"),
            vendorNameDb = json.optString("vendorNameDb", "Vendor DB"),
            productIdHex = json.optString("productIdHex", "0x0000"),
            productNameReported = json.optString("productNameReported", "Product"),
            productNameDb = json.optString("productNameDb", "Product DB"),
            interfaces = interfacesList,
            configurations = configsList
        )
    }
}
