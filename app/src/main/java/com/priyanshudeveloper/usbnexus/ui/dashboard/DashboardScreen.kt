package com.priyanshudeveloper.usbnexus.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priyanshudeveloper.usbnexus.phoenixengine.data.local.UsbNexusDataStore
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.ConnectedDevice
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.ConnectionType
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.DeviceProfile
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.ConnectedDeviceManager
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.DacKeepAliveService
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.UsbDeviceManager
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.UsbHardwareCommunicator
import com.priyanshudeveloper.usbnexus.ui.components.UsbNexusLogo
import com.priyanshudeveloper.usbnexus.ui.dashboard.components.ConnectedDeviceDetailDialog
import com.priyanshudeveloper.usbnexus.ui.theme.CardBorder
import com.priyanshudeveloper.usbnexus.ui.theme.DarkBackground
import com.priyanshudeveloper.usbnexus.ui.theme.DarkSurface
import com.priyanshudeveloper.usbnexus.ui.theme.DarkSurfaceVariant
import com.priyanshudeveloper.usbnexus.ui.theme.ElectricCyan
import com.priyanshudeveloper.usbnexus.ui.theme.StatusSuccessGreen
import com.priyanshudeveloper.usbnexus.ui.theme.StatusWarningYellow
import com.priyanshudeveloper.usbnexus.ui.theme.TechBlue
import com.priyanshudeveloper.usbnexus.ui.theme.TextDim
import com.priyanshudeveloper.usbnexus.ui.theme.TextPrimary
import com.priyanshudeveloper.usbnexus.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    deviceProfile: DeviceProfile?,
    manufacturer: String,
    model: String,
    portType: UsbPortType,
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val usbDeviceManager = remember { UsbDeviceManager(context) }
    val connectedDeviceManager = remember { ConnectedDeviceManager(context) }
    val usbHardwareCommunicator = remember { UsbHardwareCommunicator(context) }

    val connectedUsbDevices by usbDeviceManager.connectedDevices.collectAsState()
    val allConnectedDevices by connectedDeviceManager.connectedDevices.collectAsState()
    val isHostSupported by usbDeviceManager.isHostSupported.collectAsState()
    val networkStatus by usbDeviceManager.networkStatus.collectAsState()

    val isUsbDeviceConnected = allConnectedDevices.isNotEmpty()

    var selectedDeviceForDetail by remember { mutableStateOf<ConnectedDevice?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val dataStore = remember { UsbNexusDataStore(context) }
    val isDacKeepAliveActive by dataStore.dacKeepAliveEnabled.collectAsState(initial = false)

    // Sync DAC Anti-Disconnect service lifecycle based on user settings & connection state
    LaunchedEffect(isDacKeepAliveActive, isUsbDeviceConnected) {
        if (isDacKeepAliveActive && isUsbDeviceConnected) {
            DacKeepAliveService.startService(context)
        } else {
            DacKeepAliveService.stopService(context)
        }
    }

    LaunchedEffect(isUsbDeviceConnected) {
        if (allConnectedDevices.isEmpty()) {
            searchQuery = ""
        }
    }

    val filteredConnectedDevices = remember(allConnectedDevices, searchQuery) {
        if (searchQuery.isBlank()) {
            allConnectedDevices
        } else {
            allConnectedDevices.filter { dev ->
                dev.name.contains(searchQuery, ignoreCase = true) ||
                        dev.primaryDescription.contains(searchQuery, ignoreCase = true) ||
                        dev.vendorOrMacOrIp.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    DisposableEffect(Unit) {
        usbDeviceManager.startListening()
        connectedDeviceManager.startListening()
        usbHardwareCommunicator.startListeningForPermissions()
        onDispose {
            usbDeviceManager.stopListening()
            connectedDeviceManager.stopListening()
            usbHardwareCommunicator.stopListeningForPermissions()
        }
    }

    val themeAccentColor = ElectricCyan
    val themeBorderColor = TechBlue.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Dashboard Header with SETTINGS Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UsbNexusLogo(
                        size = 40.dp,
                        showBorderFrame = false,
                        showText = false
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "USB NEXUS",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Powered by Phoenix Engine",
                            color = themeAccentColor,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }

                // Settings Button in Top-Right Upright Corner
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .clickable { onNavigateToSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚙️", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Active Device Identification Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, themeBorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "UNIVERSAL DEVICE IDENTIFICATION AREA",
                        color = themeAccentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val displayName = deviceProfile?.marketingName ?: "$manufacturer $model"
                    Text(
                        text = displayName,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val modelText = deviceProfile?.model ?: model
                    val sourceText = deviceProfile?.source
                    val sourceSuffix = if (sourceText != null && sourceText != "User Selection") " ($sourceText)" else ""
                    Text(
                        text = "Model: $modelText$sourceSuffix",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(color = CardBorder)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Port Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "USB Port Configuration",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        val portName = when (portType) {
                            UsbPortType.MICRO_USB -> "Micro-USB"
                            UsbPortType.TYPE_C -> "USB Type-C"
                            UsbPortType.UNKNOWN -> "Unknown"
                        }
                        Text(
                            text = portName,
                            color = if (portType == UsbPortType.UNKNOWN) StatusWarningYellow else themeAccentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Ethernet Network Connection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🌐 Network / Ethernet State",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = networkStatus.activeTransportType,
                            color = if (networkStatus.isEthernetConnected) StatusSuccessGreen else themeAccentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // USB Host Feature
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "USB Host Feature",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isHostSupported) "Supported ✓" else "Not Supported ✕",
                            color = if (isHostSupported) StatusSuccessGreen else StatusWarningYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Connected USB / Ethernet Peripherals
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Connected Peripherals",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        val connText = if (connectedUsbDevices.isNotEmpty()) {
                            "${connectedUsbDevices.size} Device(s) Attached"
                        } else {
                            "No external device"
                        }
                        Text(
                            text = connText,
                            color = if (connectedUsbDevices.isNotEmpty()) StatusSuccessGreen else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (connectedUsbDevices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        connectedUsbDevices.forEach { dev ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceVariant, shape = RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "${dev.productName ?: dev.deviceName} (${dev.deviceClassDescription})",
                                        color = themeAccentColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Vendor ID: 0x${dev.vendorId.toString(16).uppercase()} | Product ID: 0x${dev.productId.toString(16).uppercase()} | Interfaces: ${dev.interfaceCount}",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Connected Devices Section right above PLANNED MODULES (V0.2+)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, themeBorderColor, RoundedCornerShape(12.dp)),
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
                            text = "CONNECTED DEVICES\nWITH DETAILED INFORMATION",
                            color = TechBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(themeAccentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, themeAccentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${allConnectedDevices.size} ACTIVE",
                                color = themeAccentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // SEARCH & FILTER BAR
                    val isSearchActive = allConnectedDevices.isNotEmpty()
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        enabled = isSearchActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        placeholder = {
                            Text(
                                text = if (isSearchActive) {
                                    "Filter by device name, VID/PID, or hardware..."
                                } else {
                                    "Search inactive (No device connected)"
                                },
                                color = if (isSearchActive) TextDim else TextDim.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeAccentColor,
                            unfocusedBorderColor = CardBorder,
                            disabledBorderColor = CardBorder.copy(alpha = 0.3f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            disabledTextColor = TextDim.copy(alpha = 0.5f),
                            cursorColor = themeAccentColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (filteredConnectedDevices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) {
                                    "No external devices currently connected. Connect a USB OTG peripheral to view online verified details."
                                } else {
                                    "No USB devices match filter '$searchQuery'."
                                },
                                color = TextDim,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredConnectedDevices.forEach { dev ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer { clip = true }
                                        .clickable { selectedDeviceForDetail = dev }
                                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
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
                                            val connIcon = when (dev.connectionType) {
                                                ConnectionType.USB -> "🔌"

                                            }
                                            Text(text = connIcon, fontSize = 20.sp)
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
                                                    if (dev.isVerifiedOnline) {
                                                        Text(
                                                            text = "VERIFIED ✓",
                                                            color = StatusSuccessGreen,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = FontFamily.Monospace,
                                                            modifier = Modifier
                                                                .background(StatusSuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "${dev.primaryDescription} | ${dev.vendorOrMacOrIp}",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = "Tap to view every verified detail from internet ›",
                                                    color = themeAccentColor,
                                                    fontSize = 10.sp,
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

        // Show detail dialog on device tap
        selectedDeviceForDetail?.let { device ->
            ConnectedDeviceDetailDialog(
                device = device,
                usbHardwareCommunicator = usbHardwareCommunicator,
                onDismiss = { selectedDeviceForDetail = null }
            )
        }
    }
}
