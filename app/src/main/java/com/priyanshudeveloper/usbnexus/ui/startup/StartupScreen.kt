package com.priyanshudeveloper.usbnexus.ui.startup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType
import com.priyanshudeveloper.usbnexus.phoenixengine.viewmodel.StartupStage
import com.priyanshudeveloper.usbnexus.phoenixengine.viewmodel.StartupViewModel
import com.priyanshudeveloper.usbnexus.ui.components.UsbNexusLogo
import com.priyanshudeveloper.usbnexus.ui.startup.components.AdvancedPcbCanvas
import com.priyanshudeveloper.usbnexus.ui.startup.components.ModuleStatus
import com.priyanshudeveloper.usbnexus.ui.startup.components.ResultBadge
import com.priyanshudeveloper.usbnexus.ui.startup.components.SocChipView
import com.priyanshudeveloper.usbnexus.ui.startup.components.UsbHardwareIdentifierAndSecurityMonitorModuleCard
import com.priyanshudeveloper.usbnexus.ui.theme.ElectricCyan
import com.priyanshudeveloper.usbnexus.ui.theme.TechBlue
import com.priyanshudeveloper.usbnexus.ui.theme.TextSecondary

@Composable
fun StartupScreen(
    viewModel: StartupViewModel,
    onStartupFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.stage == StartupStage.COMPLETED) {
        onStartupFinished()
    }

    val isTraceActive = state.stage == StartupStage.ANIMATING_TRACES || state.stage == StartupStage.SHOWING_RESULT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // USB Hardware Identifier Header
            Text(
                text = "USB NEXUS HARDWARE IDENTIFIER & SECURITY MONITOR ENGINE",
                color = ElectricCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )

            val stageDesc = when (state.stage) {
                StartupStage.INITIALIZING -> "Initializing System..."
                StartupStage.DETECTING_DEVICE -> "Select USB Port Type Below..."
                StartupStage.IDENTIFYING_DEVICE -> "Configured: ${state.portType.name}"
                StartupStage.ANIMATING_TRACES -> "Tracing ${state.pinCount}-Pin Physical Connector Lines Upward..."
                StartupStage.SHOWING_RESULT -> state.resultMessage
                StartupStage.COMPLETED -> "Complete"
            }

            Text(
                text = stageDesc,
                color = TextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 1.dp, bottom = 2.dp)
            )

            // Main Hardware Identifier Board Area with Unobscured SoC in Center
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Background PCB Circuit Traces
                AdvancedPcbCanvas(
                    pinCount = state.pinCount,
                    portType = state.portType,
                    progress = state.animationProgress,
                    isActive = isTraceActive
                )

                // Crystal Clear Central SoC Chip
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    SocChipView(
                        chipSize = 92.dp,
                        isActive = isTraceActive,
                        label = "SoC",
                        subLabel = "USB NEXUS"
                    )
                }

                // Far-Left Side Cards (5.0V, Trust, USB Hardware)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 2.dp, top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UsbHardwareIdentifierAndSecurityMonitorModuleCard(
                        modifier = Modifier.width(114.dp),
                        icon = "⚡",
                        title = "5.0V",
                        subtitle = "(Supply)",
                        status = if (state.stage >= StartupStage.IDENTIFYING_DEVICE) ModuleStatus.VERIFIED else ModuleStatus.INACTIVE
                    )
                    UsbHardwareIdentifierAndSecurityMonitorModuleCard(
                        modifier = Modifier.width(114.dp),
                        icon = "🛡️",
                        title = "Trust",
                        subtitle = if (state.stage >= StartupStage.ANIMATING_TRACES) "(Verified)" else "(Pending)",
                        status = if (state.stage >= StartupStage.ANIMATING_TRACES) ModuleStatus.VERIFIED else ModuleStatus.IN_PROGRESS
                    )
                    UsbHardwareIdentifierAndSecurityMonitorModuleCard(
                        modifier = Modifier.width(114.dp),
                        icon = "📈",
                        title = "USB Hardware",
                        subtitle = if (state.stage >= StartupStage.SHOWING_RESULT) "(Safe)" else "(Init)",
                        status = if (state.stage >= StartupStage.SHOWING_RESULT) ModuleStatus.VERIFIED else ModuleStatus.INACTIVE
                    )
                }

                // Far-Right Side Cards (Detect, Identify)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 2.dp, top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UsbHardwareIdentifierAndSecurityMonitorModuleCard(
                        modifier = Modifier.width(114.dp),
                        icon = "🔍",
                        title = "Detect",
                        subtitle = "(Host Ready)",
                        status = if (state.stage >= StartupStage.DETECTING_DEVICE) ModuleStatus.VERIFIED else ModuleStatus.INACTIVE
                    )
                    UsbHardwareIdentifierAndSecurityMonitorModuleCard(
                        modifier = Modifier.width(114.dp),
                        icon = "🖐️",
                        title = "Identify",
                        subtitle = if (state.stage >= StartupStage.IDENTIFYING_DEVICE) "(Complete)" else "(Pending)",
                        status = if (state.stage >= StartupStage.IDENTIFYING_DEVICE) ModuleStatus.VERIFIED else ModuleStatus.IN_PROGRESS
                    )
                }

            }
        }

        // Initial Logo Overlay
        AnimatedVisibility(
            visible = state.stage == StartupStage.INITIALIZING,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF0030712)),
                contentAlignment = Alignment.Center
            ) {
                UsbNexusLogo(size = 140.dp)
            }
        }

        // First-Run USB Port Selection Pop-Up Dialog Window
        if (state.showPortSelectionDialog) {
            Dialog(
                onDismissRequest = { /* Require user choice */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, ElectricCyan, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "DEVICE CONFIGURATION",
                            color = ElectricCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )

                        Text(
                            text = "Which USB port is this device connected to?",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Please select your device's physical USB connector type to save configuration:",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Option 1: Micro-USB (5 Pins)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, TechBlue, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.onUserSelectedPortType(UsbPortType.MICRO_USB)
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "🔌",
                                    fontSize = 24.sp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Micro-USB",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Old Models are supported this USB port",
                                        color = ElectricCyan,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Option 2: Type-C (24 Pins)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, ElectricCyan, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.onUserSelectedPortType(UsbPortType.TYPE_C)
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "⚡",
                                    fontSize = 24.sp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Type-C",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Newly and fast speed USB port type",
                                        color = ElectricCyan,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Final Result Badge Overlay
        AnimatedVisibility(
            visible = state.stage == StartupStage.SHOWING_RESULT,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ResultBadge(
                isSuccess = state.isIdentificationSuccess,
                isPortUnknown = state.isPortUnknownButDeviceFound,
                deviceProfile = state.deviceProfile,
                manufacturer = state.manufacturer,
                model = state.model,
                portType = state.portType
            )
        }
    }
}
