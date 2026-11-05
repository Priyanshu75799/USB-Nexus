package com.priyanshudeveloper.usbnexus.phoenixengine.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.DeviceProfile
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType

class PortTypePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("usb_nexus_user_port_prefs", Context.MODE_PRIVATE)

    fun hasUserSelectedPort(): Boolean {
        return prefs.getBoolean("explicit_user_port_choice_saved", false)
    }

    fun getStoredPortType(): UsbPortType? {
        if (!hasUserSelectedPort()) return null
        val portTypeStr = prefs.getString("selected_port_type", null) ?: return null
        return try {
            val type = UsbPortType.valueOf(portTypeStr)
            if (type == UsbPortType.UNKNOWN) null else type
        } catch (_: Exception) {
            null
        }
    }

    fun saveSelectedPortType(portType: UsbPortType, manufacturer: String = "", model: String = "") {
        if (portType == UsbPortType.UNKNOWN) return
        val mfg = manufacturer.ifBlank { Build.MANUFACTURER ?: "Android" }
        val mdl = model.ifBlank { Build.MODEL ?: "Device" }
        prefs.edit()
            .putBoolean("explicit_user_port_choice_saved", true)
            .putString("selected_port_type", portType.name)
            .putString("cached_port_type", portType.name)
            .putString("cached_manufacturer", mfg)
            .putString("cached_model", mdl)
            .putString("cached_marketing_name", "$mfg $mdl".trim())
            .putString("cached_source", "User Selection")
            .putString("cached_confidence", "User Verified")
            .apply()
    }

    fun clearUserSelection() {
        prefs.edit().clear().apply()
    }

    fun getStoredDeviceProfile(): DeviceProfile? {
        val portType = getStoredPortType() ?: return null

        val manufacturer = prefs.getString("cached_manufacturer", Build.MANUFACTURER ?: "Android") ?: "Android"
        val model = prefs.getString("cached_model", Build.MODEL ?: "Device") ?: "Device"
        val marketingName = prefs.getString("cached_marketing_name", "$manufacturer $model")

        return DeviceProfile(
            manufacturer = manufacturer,
            model = model,
            marketingName = marketingName,
            usbPortType = portType,
            usbHostSupported = true,
            otgSupported = true,
            source = "User Selection",
            confidence = "User Verified"
        )
    }

    fun saveDeviceProfile(profile: DeviceProfile) {
        saveSelectedPortType(
            portType = profile.usbPortType,
            manufacturer = profile.manufacturer,
            model = profile.model
        )
    }
}
