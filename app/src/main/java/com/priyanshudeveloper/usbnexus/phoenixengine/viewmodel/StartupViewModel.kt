package com.priyanshudeveloper.usbnexus.phoenixengine.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.priyanshudeveloper.usbnexus.phoenixengine.data.repository.DeviceProfileRepositoryImpl
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.DeviceProfile
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.DeviceProfilePortTypeProvider
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.PortTypeProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StartupStage {
    INITIALIZING,
    DETECTING_DEVICE,
    IDENTIFYING_DEVICE,
    ANIMATING_TRACES,
    SHOWING_RESULT,
    COMPLETED
}

data class StartupUiState(
    val stage: StartupStage = StartupStage.INITIALIZING,
    val manufacturer: String = "",
    val model: String = "",
    val deviceProfile: DeviceProfile? = null,
    val portType: UsbPortType = UsbPortType.UNKNOWN,
    val pinCount: Int = 0,
    val showPortSelectionDialog: Boolean = false,
    val isIdentificationSuccess: Boolean = false,
    val isPortUnknownButDeviceFound: Boolean = false,
    val resultMessage: String = "",
    val animationProgress: Float = 0f
)

class StartupViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val portTypeProvider: PortTypeProvider =
        DeviceProfilePortTypeProvider(DeviceProfileRepositoryImpl(application))

    private val _uiState = MutableStateFlow(StartupUiState())
    val uiState: StateFlow<StartupUiState> = _uiState.asStateFlow()

    init {
        runStartupSequence()
    }

    fun runStartupSequence() {
        viewModelScope.launch {
            val mfg = Build.MANUFACTURER ?: "Android"
            val mdl = Build.MODEL ?: "Device"

            _uiState.update {
                it.copy(
                    stage = StartupStage.INITIALIZING,
                    manufacturer = mfg,
                    model = mdl
                )
            }

            // Check if user has explicitly saved a port selection in PortTypePreferences
            val storedProfile = portTypeProvider.getDeviceProfile()

            if (storedProfile != null && storedProfile.usbPortType != UsbPortType.UNKNOWN) {
                // Choice already saved! Proceed directly with stored port type
                delay(300)
                proceedWithPortType(storedProfile)
            } else {
                // First launch / No selection yet: Open Pop-Up Window Dialog immediately!
                _uiState.update {
                    it.copy(
                        stage = StartupStage.DETECTING_DEVICE,
                        showPortSelectionDialog = true
                    )
                }
            }
        }
    }

    fun onUserSelectedPortType(selectedPortType: UsbPortType) {
        viewModelScope.launch {
            _uiState.update { it.copy(showPortSelectionDialog = false) }
            val savedProfile = portTypeProvider.saveSelectedPortType(selectedPortType)
            proceedWithPortType(savedProfile)
        }
    }

    private suspend fun proceedWithPortType(profile: DeviceProfile) {
        val portType = profile.usbPortType
        val pinCount = if (portType == UsbPortType.TYPE_C) 24 else 5

        _uiState.update {
            it.copy(
                stage = StartupStage.IDENTIFYING_DEVICE,
                deviceProfile = profile,
                portType = portType,
                pinCount = pinCount,
                isIdentificationSuccess = true,
                resultMessage = "Device Configured (${if (portType == UsbPortType.TYPE_C) "Type-C 24-Pin" else "Micro-USB 5-Pin"})"
            )
        }
        delay(400)

        // STEP 4: ANIMATING_TRACES
        _uiState.update { it.copy(stage = StartupStage.ANIMATING_TRACES) }
        val steps = 40
        for (i in 1..steps) {
            delay(25)
            val progress = i / steps.toFloat()
            _uiState.update { it.copy(animationProgress = progress) }
        }

        // Directly transition to COMPLETED (Dashboard) after animation finishes
        delay(100)
        _uiState.update { it.copy(stage = StartupStage.COMPLETED) }
    }
}
