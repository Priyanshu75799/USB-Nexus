package com.priyanshudeveloper.usbnexus.phoenixengine.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "usb_nexus_settings")

class UsbNexusDataStore(private val context: Context) {

    companion object {
        val KEY_SECURITY_ALERTS_ENABLED = booleanPreferencesKey("security_alerts_enabled")
        val KEY_SAVED_DEVICE_PROFILE_JSON = stringPreferencesKey("saved_device_profile_json")
        val KEY_USB_HISTORY_JSON = stringPreferencesKey("usb_history_json")
        val KEY_DAC_KEEP_ALIVE_ENABLED = booleanPreferencesKey("dac_keep_alive_enabled")
        val KEY_SAVE_USB_INFO_ENABLED = booleanPreferencesKey("save_usb_info_enabled")
    }

    val securityAlertsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SECURITY_ALERTS_ENABLED] ?: true
    }

    suspend fun setSecurityAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SECURITY_ALERTS_ENABLED] = enabled
        }
    }

    val savedDeviceProfileJson: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_SAVED_DEVICE_PROFILE_JSON]
    }

    suspend fun saveDeviceProfileJson(json: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SAVED_DEVICE_PROFILE_JSON] = json
        }
    }

    val usbHistoryJson: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USB_HISTORY_JSON]
    }

    suspend fun saveUsbHistoryJson(json: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USB_HISTORY_JSON] = json
        }
    }

    val dacKeepAliveEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DAC_KEEP_ALIVE_ENABLED] ?: false
    }

    suspend fun setDacKeepAliveEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DAC_KEEP_ALIVE_ENABLED] = enabled
        }
    }

    val saveUsbInfoEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SAVE_USB_INFO_ENABLED] ?: false
    }

    suspend fun setSaveUsbInfoEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SAVE_USB_INFO_ENABLED] = enabled
        }
    }
}
