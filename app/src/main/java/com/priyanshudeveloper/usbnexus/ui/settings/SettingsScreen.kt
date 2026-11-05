package com.priyanshudeveloper.usbnexus.ui.settings

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch

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
            // Header Row with Back Button & Centered Title
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
                    text = "SETTINGS",
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

            // 1. UNIVERSAL USB DAC ANTI-DISCONNECT
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
                                    text = "⚡ UNIVERSAL USB DAC\n   ANTI-DISCONNECT",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isDacKeepAliveActive && isUsbDeviceConnected) {
                                        "ACTIVE ✓"
                                    } else if (isDacKeepAliveActive) {
                                        "READY"
                                    } else {
                                        "OFF"
                                    },
                                    color = if (isDacKeepAliveActive && isUsbDeviceConnected) StatusSuccessGreen else if (isDacKeepAliveActive) TechBlue else TextDim,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(
                                            if (isDacKeepAliveActive && isUsbDeviceConnected) StatusSuccessGreen.copy(alpha = 0.15f) else DarkSurfaceVariant,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isDacKeepAliveActive,
                            enabled = true,
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
                        text = "Keeps high-resolution Type-C USB DACs, amplifiers, and audio adapters active when your phone screen is locked or music is paused. By transmitting a silent 0dB signal, it prevents the Linux OTG kernel from suspending USB power rails, eliminating audio pops and disconnects.",
                        color = TextSecondary,
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
                                    text = "💾 SAVING INFORMATION OF\n   CONNECTED USB DEVICES",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSaveUsbInfoEnabled) "ACTIVE ✓" else "OFF",
                                    color = if (isSaveUsbInfoEnabled) StatusSuccessGreen else TextDim,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(
                                            if (isSaveUsbInfoEnabled) StatusSuccessGreen.copy(alpha = 0.15f) else DarkSurfaceVariant,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
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

                    // Button appears below block ONLY when sliding switch is ON!
                    if (isSaveUsbInfoEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onNavigateToHistory() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "📂 VIEW SAVED USB LOGS & HISTORY",
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
        }
    }
}
