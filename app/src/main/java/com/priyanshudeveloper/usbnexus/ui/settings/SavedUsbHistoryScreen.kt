package com.priyanshudeveloper.usbnexus.ui.settings

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priyanshudeveloper.usbnexus.phoenixengine.data.local.ConnectedDeviceCache
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.ConnectedDevice
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.UsbHardwareCommunicator
import com.priyanshudeveloper.usbnexus.ui.dashboard.components.ConnectedDeviceDetailDialog
import com.priyanshudeveloper.usbnexus.ui.theme.CardBorder
import com.priyanshudeveloper.usbnexus.ui.theme.DarkBackground
import com.priyanshudeveloper.usbnexus.ui.theme.DarkSurface
import com.priyanshudeveloper.usbnexus.ui.theme.DarkSurfaceVariant
import com.priyanshudeveloper.usbnexus.ui.theme.ElectricCyan
import com.priyanshudeveloper.usbnexus.ui.theme.StatusErrorRed
import com.priyanshudeveloper.usbnexus.ui.theme.StatusSuccessGreen
import com.priyanshudeveloper.usbnexus.ui.theme.TechBlue
import com.priyanshudeveloper.usbnexus.ui.theme.TextDim
import com.priyanshudeveloper.usbnexus.ui.theme.TextPrimary
import com.priyanshudeveloper.usbnexus.ui.theme.TextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedUsbHistoryScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val deviceCache = remember { ConnectedDeviceCache(context) }
    val usbHardwareCommunicator = remember { UsbHardwareCommunicator(context) }

    var savedDevices by remember { mutableStateOf(deviceCache.getSavedUsbDevices()) }
    var selectedDeviceForDetail by remember { mutableStateOf<ConnectedDevice?>(null) }
    var devicePendingDeleteSet by remember { mutableStateOf(setOf<String>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    if (devicePendingDeleteSet.isNotEmpty()) {
                        devicePendingDeleteSet = emptySet()
                    }
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row with BACK Button on Top-Left
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .clickable { onNavigateBack() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "BACK",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "SAVED USB DEVICE HISTORY",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TechBlue.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECORDED USB LOGS (${savedDevices.size})",
                            color = TechBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(ElectricCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "SAVED SPECS",
                                color = ElectricCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (savedDevices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No USB devices logged yet. Connect a USB device while 'Saving Information Of Connected USB Devices' feature is active.",
                                color = TextDim,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            savedDevices.forEach { dev ->
                                val isDeleteButtonVisible = devicePendingDeleteSet.contains(dev.id)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                if (isDeleteButtonVisible) {
                                                    // Unselect / Cancel delete mode when tapped again
                                                    devicePendingDeleteSet = devicePendingDeleteSet - dev.id
                                                } else if (devicePendingDeleteSet.isNotEmpty()) {
                                                    // Unselect all if another card was selected
                                                    devicePendingDeleteSet = emptySet()
                                                } else {
                                                    // Open specs window in normal state only
                                                    selectedDeviceForDetail = dev
                                                }
                                            },
                                            onLongClick = {
                                                // Long-press (3 seconds) triggers Red Delete Button & selects for deletion
                                                devicePendingDeleteSet = devicePendingDeleteSet + dev.id
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isDeleteButtonVisible) StatusErrorRed else CardBorder,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDeleteButtonVisible) StatusErrorRed.copy(alpha = 0.08f) else DarkSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "🔌", fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = dev.name,
                                                        color = TextPrimary,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "LOGGED ✓",
                                                        color = StatusSuccessGreen,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace,
                                                        modifier = Modifier
                                                            .background(StatusSuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "${dev.primaryDescription} | ${dev.vendorOrMacOrIp}",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = if (isDeleteButtonVisible) "Tap red trash button to delete entry" else "Tap for detailed specs | Long-press (3s) to delete ›",
                                                    color = if (isDeleteButtonVisible) StatusErrorRed else ElectricCyan,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }

                                        // Red Delete Button in Top-Right Corner upon 3-second long press
                                        if (isDeleteButtonVisible) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(StatusErrorRed, RoundedCornerShape(6.dp))
                                                    .border(1.dp, StatusErrorRed, RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        deviceCache.removeDeviceFromHistory(dev.id)
                                                        savedDevices = deviceCache.getSavedUsbDevices()
                                                        devicePendingDeleteSet = devicePendingDeleteSet - dev.id
                                                        Toast.makeText(context, "Deleted '${dev.name}' from history 🗑️", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "🗑DELETE",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Detailed hardware spec pop-up window (Same as Homepage, without hardware permission/DAC toggle)
        selectedDeviceForDetail?.let { device ->
            ConnectedDeviceDetailDialog(
                device = device,
                usbHardwareCommunicator = usbHardwareCommunicator,
                showHardwareControls = false,
                onDismiss = { selectedDeviceForDetail = null }
            )
        }
    }
}
