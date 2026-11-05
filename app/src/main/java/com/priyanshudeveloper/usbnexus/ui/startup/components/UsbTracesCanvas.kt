package com.priyanshudeveloper.usbnexus.ui.startup.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType

@Composable
fun UsbTracesCanvas(
    modifier: Modifier = Modifier,
    pinCount: Int = 5,
    portType: UsbPortType = UsbPortType.MICRO_USB,
    progress: Float = 0f
) {
    AdvancedPcbCanvas(
        modifier = modifier,
        pinCount = pinCount,
        portType = portType,
        progress = progress,
        isActive = progress > 0f
    )
}
