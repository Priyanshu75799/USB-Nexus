package com.priyanshudeveloper.usbnexus.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.ConnectedDevice
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.ConnectionType
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.DeviceProfile
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.ConnectedDeviceManager
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.DacKeepAliveService
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.UsbDeviceManager
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.UsbHardwareCommunicator
import com.priyanshudeveloper.usbnexus.ui.components.UsbNexusLogo
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.ConnectedDeviceDetailDialog
import com.priyanshudeveloper.usbnexus.ui.theme.CardBorder
import com.priyanshudeveloper.usbnexus.ui.theme.DarkBackground
import com.priyanshudeveloper.usbnexus.ui.theme.DarkSurface
import com.priyanshudeveloper.usbnexus.ui.theme.DarkSurfaceVariant
import com.priyanshudeveloper.usbnexus.ui.theme.ElectricCyan
import com.priyanshudeveloper.usbnexus.ui.theme.StatusErrorRed
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
    val lifecycleOwner = LocalLifecycleOwner.current

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

    // Permissions States
    var isBatteryOptDisabled by remember { mutableStateOf(checkBatteryOptimizationDisabled(context)) }
    var isNotificationGranted by remember { mutableStateOf(checkNotificationPermissionGranted(context)) }
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var showPermissionInfoDialog by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isNotificationGranted = isGranted || checkNotificationPermissionGranted(context)
    }

    // Refresh permissions on Resume
    DisposableEffect(lifecycleOwner) {
        usbDeviceManager.startListening()
        connectedDeviceManager.startListening()
        usbHardwareCommunicator.startListeningForPermissions()

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryOptDisabled = checkBatteryOptimizationDisabled(context)
                isNotificationGranted = checkNotificationPermissionGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            usbDeviceManager.stopListening()
            connectedDeviceManager.stopListening()
            usbHardwareCommunicator.stopListeningForPermissions()
        }
    }

    val isAllPermissionsGranted = isBatteryOptDisabled && isNotificationGranted

    // Stop DAC Anti-Suspend service when USB device disconnects (Do NOT auto-start on USB connect)
    LaunchedEffect(isUsbDeviceConnected) {
        if (!isUsbDeviceConnected) {
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
            // Dashboard Header with CAPSULE BUTTONS (Permissions & SETTINGS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UsbNexusLogo(
                        size = 38.dp,
                        showBorderFrame = false,
                        showText = false
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "USB Nexus",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Powered by Phoenix Engine",
                            color = themeAccentColor,
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Permissions Capsule Button (Red Theme if missing, App Theme if granted)
                    val permBg = if (isAllPermissionsGranted) DarkSurfaceVariant else StatusErrorRed.copy(alpha = 0.15f)
                    val permBorder = if (isAllPermissionsGranted) CardBorder else StatusErrorRed
                    val permText = if (isAllPermissionsGranted) ElectricCyan else StatusErrorRed

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(permBg)
                            .border(1.dp, permBorder, RoundedCornerShape(50.dp))
                            .clickable { showPermissionsDialog = true }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Permission",
                                color = permText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // 2. Settings Capsule Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, CardBorder, RoundedCornerShape(50.dp))
                            .clickable { onNavigateToSettings() }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Settings",
                            color = ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
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
                        text = "Universal Device Identification Area",
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
                            text = if (isHostSupported) "Supported" else "Not Supported",
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
                            text = "Connected Devices\nWith Detailed Information",
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
                                .background(themeAccentColor.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                                .border(1.dp, themeAccentColor.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${allConnectedDevices.size} Active",
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
                        shape = RoundedCornerShape(50.dp)
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
                                                    if (dev.isVerifiedOnline) {
                                                        Text(
                                                            text = "Verified",
                                                            color = StatusSuccessGreen,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = FontFamily.Monospace,
                                                            modifier = Modifier
                                                                .background(StatusSuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
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

        // =========================================================================
        // PERMISSIONS SCROLLABLE POP-UP WINDOW DIALOG
        // =========================================================================
        if (showPermissionsDialog) {
            val mainCardBorderColor = if (isAllPermissionsGranted) TechBlue.copy(alpha = 0.6f) else StatusErrorRed.copy(alpha = 0.85f)
            val mainCardHeaderColor = if (isAllPermissionsGranted) ElectricCyan else StatusErrorRed

            Dialog(onDismissRequest = { showPermissionsDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, mainCardBorderColor, RoundedCornerShape(16.dp)),
                    color = DarkBackground
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header Row with Title & clickable ⓘ Info icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Permissions",
                                    color = mainCardHeaderColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isAllPermissionsGranted) "All Granted" else "Action Required",
                                    color = if (isAllPermissionsGranted) StatusSuccessGreen else StatusErrorRed,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(
                                            if (isAllPermissionsGranted) StatusSuccessGreen.copy(alpha = 0.15f) else StatusErrorRed.copy(alpha = 0.15f),
                                            RoundedCornerShape(50.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Info ⓘ Button
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (isAllPermissionsGranted) DarkSurfaceVariant else StatusErrorRed.copy(alpha = 0.2f),
                                            CircleShape
                                        )
                                        .border(
                                            1.dp,
                                            if (isAllPermissionsGranted) TechBlue else StatusErrorRed,
                                            CircleShape
                                        )
                                        .clickable { showPermissionInfoDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "ⓘ",
                                        color = if (isAllPermissionsGranted) ElectricCyan else StatusErrorRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Close Button
                                Box(
                                    modifier = Modifier
                                        .background(DarkSurfaceVariant, CircleShape)
                                        .clickable { showPermissionsDialog = false }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "✕", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = CardBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Sub-Block 1: Disable Battery Optimisation Button
                        val sub1Bg = if (isBatteryOptDisabled) DarkSurfaceVariant else StatusErrorRed.copy(alpha = 0.12f)
                        val sub1Border = if (isBatteryOptDisabled) CardBorder else StatusErrorRed.copy(alpha = 0.8f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(sub1Bg)
                                .border(1.dp, sub1Border, RoundedCornerShape(12.dp))
                                .clickable { launchBatteryOptimizationSettings(context) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Disable Battery Optimisation",
                                        color = if (isBatteryOptDisabled) TextPrimary else StatusErrorRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (isBatteryOptDisabled) "Unrestricted background USB process" else "Tap to grant unrestricted background access",
                                        color = if (isBatteryOptDisabled) TextSecondary else StatusErrorRed.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBatteryOptDisabled) "Granted" else "Required",
                                    color = if (isBatteryOptDisabled) StatusSuccessGreen else StatusErrorRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(
                                            if (isBatteryOptDisabled) StatusSuccessGreen.copy(alpha = 0.15f) else StatusErrorRed.copy(alpha = 0.2f),
                                            RoundedCornerShape(50.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isBatteryOptDisabled) StatusSuccessGreen else StatusErrorRed,
                                            RoundedCornerShape(50.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sub-Block 2: Notification Button
                        val sub2Bg = if (isNotificationGranted) DarkSurfaceVariant else StatusErrorRed.copy(alpha = 0.12f)
                        val sub2Border = if (isNotificationGranted) CardBorder else StatusErrorRed.copy(alpha = 0.8f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(sub2Bg)
                                .border(1.dp, sub2Border, RoundedCornerShape(12.dp))
                                .clickable { launchNotificationPermissionSettings(context) { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) } }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Notification",
                                        color = if (isNotificationGranted) TextPrimary else StatusErrorRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (isNotificationGranted) "Instant USB connect alerts enabled" else "Tap to allow real-time USB connect alerts",
                                        color = if (isNotificationGranted) TextSecondary else StatusErrorRed.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isNotificationGranted) "Granted" else "Required",
                                    color = if (isNotificationGranted) StatusSuccessGreen else StatusErrorRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(
                                            if (isNotificationGranted) StatusSuccessGreen.copy(alpha = 0.15f) else StatusErrorRed.copy(alpha = 0.2f),
                                            RoundedCornerShape(50.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isNotificationGranted) StatusSuccessGreen else StatusErrorRed,
                                            RoundedCornerShape(50.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showPermissionsDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = "Done",
                                color = DarkBackground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // PERMISSIONS INFO ⓘ POP-UP DIALOG WINDOW
        if (showPermissionInfoDialog) {
            Dialog(onDismissRequest = { showPermissionInfoDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.72f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, ElectricCyan, RoundedCornerShape(16.dp)),
                    color = DarkBackground
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Permissions Guide",
                                color = ElectricCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.2.sp
                            )
                            Box(
                                modifier = Modifier
                                    .background(DarkSurfaceVariant, CircleShape)
                                    .clickable { showPermissionInfoDialog = false }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "✕", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = CardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // 1. Disable Battery Optimisation Explanation
                        Text(
                            text = "1. Disable Battery Optimisation",
                            color = TechBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Why is it required?\nAndroid OS power management (Doze Mode & App Standby) aggressively kills background tasks when the phone screen turns off. Granting 'Unrestricted' battery access ensures USB Nexus can instantly detect OTG peripherals and notify you without delay or system background kills. How to disable Battery Optimisation?\n click on 'Disable Battery Optimisation' > Click on 'Battery' section or Battery related word > Choose 'Unrestricted' or 'Allow to run in background'",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Notification Explanation
                        Text(
                            text = "2. Notification Permission",
                            color = TechBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Why is it required?\nOn Android 13+ (API 33+), runtime notification permission is required to post real-time alerts. Allowing notifications enables USB Nexus to immediately alert you with high priority (heads-up banner) whenever any USB hardware or OTG device is connected to your smartphone.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showPermissionInfoDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = "Understood",
                                color = DarkBackground,
                                fontSize = 11.sp,
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

private fun checkBatteryOptimizationDisabled(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
    } else {
        true
    }
}

private fun checkNotificationPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun launchBatteryOptimizationSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallbackIntent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            context.startActivity(fallbackIntent)
        } catch (_: Exception) {}
    }
}

private fun launchNotificationPermissionSettings(
    context: Context,
    requestRuntimePermission: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        requestRuntimePermission()
    } else {
        try {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
}
