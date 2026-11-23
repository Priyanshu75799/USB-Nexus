package com.priyanshudeveloper.usbnexus.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priyanshudeveloper.usbnexus.phoenixengine.data.local.UsbNexusDataStore
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.ConnectedDeviceManager
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.DacKeepAliveService
import com.priyanshudeveloper.usbnexus.ui.theme.CardBorder
import com.priyanshudeveloper.usbnexus.ui.theme.DarkBackground
import com.priyanshudeveloper.usbnexus.ui.theme.DarkSurface
import com.priyanshudeveloper.usbnexus.ui.theme.DarkSurfaceVariant
import com.priyanshudeveloper.usbnexus.ui.theme.ElectricCyan
import com.priyanshudeveloper.usbnexus.ui.theme.StatusSuccessGreen
import com.priyanshudeveloper.usbnexus.ui.theme.TechBlue
import com.priyanshudeveloper.usbnexus.ui.theme.TextDim
import com.priyanshudeveloper.usbnexus.ui.theme.TextPrimary
import com.priyanshudeveloper.usbnexus.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val dataStore = remember { UsbNexusDataStore(context) }
    val connectedDeviceManager = remember { ConnectedDeviceManager(context) }

    val isDacKeepAliveActive by dataStore.dacKeepAliveEnabled.collectAsState(initial = false)
    val isSaveUsbInfoEnabled by dataStore.saveUsbInfoEnabled.collectAsState(initial = false)
    val connectedDevices by connectedDeviceManager.connectedDevices.collectAsState()

    val isUsbDeviceConnected = connectedDevices.isNotEmpty()

    // Get dynamic current app version name
    val currentVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.7"
        } catch (_: Exception) {
            "1.7"
        }
    }

    // Update Check States
    var newVersionDetected by remember { mutableStateOf<String?>(null) }
    var isUpdateAvailable by remember { mutableStateOf(false) }

    // Check GitHub Releases for version > currentVersion
    LaunchedEffect(currentVersion) {
        withContext(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://api.github.com/repos/Priyanshu75799/USB-Nexus/releases/latest")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "USBNexus-App")
                connection.connectTimeout = 4000
                connection.readTimeout = 4000

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val tagRegex = """"tag_name"\s*:\s*"([^"]+)"""".toRegex()
                    val match = tagRegex.find(response)
                    val tagName = match?.groupValues?.get(1)?.removePrefix("v")?.trim() ?: ""

                    if (tagName.isNotBlank() && isVersionGreaterThanCurrent(tagName, currentVersion)) {
                        newVersionDetected = tagName
                        isUpdateAvailable = true
                    } else {
                        isUpdateAvailable = false
                    }
                }
            } catch (_: Exception) {
                isUpdateAvailable = false
            }
        }
    }

    DisposableEffect(Unit) {
        connectedDeviceManager.startListening()
        onDispose {
            connectedDeviceManager.stopListening()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row with BACK Capsule Button & Centered Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, CardBorder, RoundedCornerShape(50.dp))
                        .clickable { onNavigateBack() }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
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
                    text = "Settings",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. UNIVERSAL USB DAC ANTI-SUSPEND
            val cardBorderColor = if (isUsbDeviceConnected) TechBlue.copy(alpha = 0.6f) else CardBorder.copy(alpha = 0.35f)
            val cardBgColor = if (isUsbDeviceConnected) DarkSurface else DarkSurface.copy(alpha = 0.5f)
            val titleColor = if (isUsbDeviceConnected) ElectricCyan else TextDim

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Universal USB DAC\nAnti-Suspend",
                                    color = titleColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (!isUsbDeviceConnected) {
                                        "No USB Connected"
                                    } else if (isDacKeepAliveActive) {
                                        "Active ✓"
                                    } else {
                                        "Off"
                                    },
                                    color = if (!isUsbDeviceConnected) TextDim else if (isDacKeepAliveActive) StatusSuccessGreen else TechBlue,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(
                                            if (isDacKeepAliveActive && isUsbDeviceConnected) StatusSuccessGreen.copy(alpha = 0.15f) else DarkSurfaceVariant,
                                            RoundedCornerShape(50.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isDacKeepAliveActive && isUsbDeviceConnected,
                            enabled = isUsbDeviceConnected,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    dataStore.setDacKeepAliveEnabled(enabled)
                                    if (enabled && isUsbDeviceConnected) {
                                        DacKeepAliveService.startService(context)
                                    } else {
                                        DacKeepAliveService.stopService(context)
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkBackground,
                                checkedTrackColor = ElectricCyan,
                                uncheckedThumbColor = TextDim,
                                uncheckedTrackColor = DarkSurfaceVariant,
                                disabledCheckedThumbColor = TextDim,
                                disabledCheckedTrackColor = DarkSurfaceVariant.copy(alpha = 0.4f),
                                disabledUncheckedThumbColor = TextDim.copy(alpha = 0.4f),
                                disabledUncheckedTrackColor = DarkSurfaceVariant.copy(alpha = 0.2f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Detailed Feature Description:",
                        color = if (isUsbDeviceConnected) TechBlue else TextDim,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isUsbDeviceConnected) {
                            "Keeps high-resolution Type-C USB DACs, amplifiers, and audio adapters active when your phone screen is locked or music is paused. By transmitting a silent 0dB signal, it prevents the Linux OTG kernel from suspending USB power rails, eliminating audio pops and disconnects."
                        } else {
                            "Feature is currently standby/greyed out because no USB OTG peripheral or DAC is connected. Connect a USB device to enable this service."
                        },
                        color = if (isUsbDeviceConnected) TextSecondary else TextDim,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. SAVING INFORMATION OF CONNECTED USB DEVICES
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Saving Information Of\nConnected USB Devices",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSaveUsbInfoEnabled) "Active" else "Off",
                                    color = if (isSaveUsbInfoEnabled) StatusSuccessGreen else TextDim,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(
                                            if (isSaveUsbInfoEnabled) StatusSuccessGreen.copy(alpha = 0.15f) else DarkSurfaceVariant,
                                            RoundedCornerShape(50.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isSaveUsbInfoEnabled,
                            enabled = true,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    dataStore.setSaveUsbInfoEnabled(enabled)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkBackground,
                                checkedTrackColor = ElectricCyan,
                                uncheckedThumbColor = TextDim,
                                uncheckedTrackColor = DarkSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Detailed Feature Description:",
                        color = TechBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Automatically records, indexes, and saves every USB hardware device, interface descriptor, and verified specifications logged after starting this service. Allows offline inspection and log history retrieval.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )

                    if (isSaveUsbInfoEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onNavigateToHistory() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = "View Saved USB Logs & History",
                                color = DarkBackground,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. DEVELOPER & GITHUB UPDATE DETECTION BLOCK
            val updateCardBorder = if (isUpdateAvailable) StatusSuccessGreen else TechBlue.copy(alpha = 0.4f)
            val updateCardBg = if (isUpdateAvailable) DarkSurface else DarkSurface.copy(alpha = 0.5f)
            val buttonBgColor = if (isUpdateAvailable) ElectricCyan else DarkSurfaceVariant
            val buttonTextColor = if (isUpdateAvailable) DarkBackground else TextDim

            SelectionContainer {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, updateCardBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = updateCardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Developer & GitHub Channel",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Developer: Priyanshu Rajput\nGitHub: Priyanshu75799/USB-Nexus\nE-mail ID: iampriyanshudeveloper@gmail.com",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isUpdateAvailable) "New Version v${newVersionDetected ?: ""} " else "Up to Date (v$currentVersion)",
                                color = if (isUpdateAvailable) StatusSuccessGreen else TextDim,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .background(
                                        if (isUpdateAvailable) StatusSuccessGreen.copy(alpha = 0.15f) else DarkSurfaceVariant,
                                        RoundedCornerShape(50.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = CardBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "If you have any issue with my app, please send an email to my Email ID and I will try to fix it in the next version of the app.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isUpdateAvailable) {
                                "A new update (v${newVersionDetected}) has been detected on GitHub! Tap the cylindrical button below to open the repository and download the latest release."
                            } else {
                                "Currently running USB Nexus v$currentVersion. Block & button will automatically activate whenever a new release (> v$currentVersion) is published on GitHub."
                            },
                            color = if (isUpdateAvailable) TextPrimary else TextDim,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Cylindrical Button (RoundedCornerShape 50.dp)
                        Button(
                            onClick = {
                                val repoUrl = "https://github.com/Priyanshu75799/USB-Nexus/releases/latest"
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            enabled = isUpdateAvailable,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonBgColor,
                                disabledContainerColor = DarkSurfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                text = if (isUpdateAvailable) "🚀 Download New Version (GitHub)" else "App Up to Date (v$currentVersion)",
                                color = buttonTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isVersionGreaterThanCurrent(latest: String, current: String): Boolean {
    try {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
    } catch (_: Exception) {}
    return false
}
