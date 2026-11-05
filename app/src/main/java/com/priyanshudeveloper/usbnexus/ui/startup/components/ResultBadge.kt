package com.priyanshudeveloper.usbnexus.ui.startup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.DeviceProfile
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType
import com.priyanshudeveloper.usbnexus.ui.theme.DarkSurface
import com.priyanshudeveloper.usbnexus.ui.theme.StatusErrorRed
import com.priyanshudeveloper.usbnexus.ui.theme.StatusSuccessGreen
import com.priyanshudeveloper.usbnexus.ui.theme.StatusWarningYellow
import com.priyanshudeveloper.usbnexus.ui.theme.TextPrimary
import com.priyanshudeveloper.usbnexus.ui.theme.TextSecondary

@Composable
fun ResultBadge(
    modifier: Modifier = Modifier,
    isSuccess: Boolean,
    isPortUnknown: Boolean = false,
    deviceProfile: DeviceProfile?,
    manufacturer: String,
    model: String,
    portType: UsbPortType
) {
    val statusColor = when {
        isSuccess -> StatusSuccessGreen
        isPortUnknown -> StatusWarningYellow
        else -> StatusErrorRed
    }

    val titleText = when {
        isSuccess -> "Device Identified"
        isPortUnknown -> "Device Identified"
        else -> "Device Identification Failed"
    }

    val portText = when (portType) {
        UsbPortType.MICRO_USB -> "Micro-USB"
        UsbPortType.TYPE_C -> "USB Type-C"
        UsbPortType.UNKNOWN -> "Unknown"
    }

    AnimatedVisibility(
        visible = true,
        enter = scaleIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
    ) {
        Box(
            modifier = modifier
                .graphicsLayer { clip = true }
                .background(DarkSurface.copy(alpha = 0.95f), shape = RoundedCornerShape(16.dp))
                .border(2.dp, statusColor, shape = RoundedCornerShape(16.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Circle Badge Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(statusColor.copy(alpha = 0.15f), shape = CircleShape)
                        .border(2.dp, statusColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(26.dp)) {
                        val strokeWidth = 3.5.dp.toPx()
                        if (isSuccess || isPortUnknown) {
                            val path = Path().apply {
                                moveTo(size.width * 0.12f, size.height * 0.52f)
                                lineTo(size.width * 0.40f, size.height * 0.78f)
                                lineTo(size.width * 0.88f, size.height * 0.22f)
                            }
                            drawPath(
                                path = path,
                                color = statusColor,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        } else {
                            val p1 = Path().apply {
                                moveTo(size.width * 0.18f, size.height * 0.18f)
                                lineTo(size.width * 0.82f, size.height * 0.82f)
                            }
                            val p2 = Path().apply {
                                moveTo(size.width * 0.82f, size.height * 0.18f)
                                lineTo(size.width * 0.18f, size.height * 0.82f)
                            }
                            drawPath(p1, color = statusColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                            drawPath(p2, color = statusColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "USB Nexus",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = titleText,
                    color = statusColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (deviceProfile != null) {
                    val displayName = deviceProfile.marketingName ?: "$manufacturer $model"
                    Text(
                        text = displayName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Model: ${deviceProfile.model}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "$manufacturer $model",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Port: $portText",
                    color = if (portType == UsbPortType.UNKNOWN) StatusWarningYellow else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
