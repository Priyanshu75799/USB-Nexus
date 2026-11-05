package com.priyanshudeveloper.usbnexus.ui.startup.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priyanshudeveloper.usbnexus.ui.theme.ElectricCyan
import com.priyanshudeveloper.usbnexus.ui.theme.StatusSuccessGreen
import com.priyanshudeveloper.usbnexus.ui.theme.StatusWarningYellow
import com.priyanshudeveloper.usbnexus.ui.theme.TechBlue
import com.priyanshudeveloper.usbnexus.ui.theme.TextDim
import com.priyanshudeveloper.usbnexus.ui.theme.TextPrimary

enum class ModuleStatus {
    INACTIVE,
    IN_PROGRESS,
    VERIFIED,
    ALERT
}

@Composable
fun UsbHardwareIdentifierAndSecurityMonitorModuleCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    subtitle: String,
    status: ModuleStatus = ModuleStatus.INACTIVE
) {
    val borderColor by animateColorAsState(
        targetValue = when (status) {
            ModuleStatus.VERIFIED -> ElectricCyan
            ModuleStatus.ALERT -> StatusWarningYellow
            ModuleStatus.IN_PROGRESS -> TechBlue.copy(alpha = 0.9f)
            ModuleStatus.INACTIVE -> TechBlue.copy(alpha = 0.35f)
        },
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "cardBorder"
    )

    val containerBg = Color(0xFB061324)

    Box(
        modifier = modifier
            .graphicsLayer { clip = true }
            .clip(RoundedCornerShape(8.dp))
            .background(containerBg)
            .border(1.1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFF0D233F), RoundedCornerShape(4.dp))
                        .border(0.8.dp, TechBlue.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 9.5.sp
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Title & Subtitle
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.2.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = when (status) {
                            ModuleStatus.VERIFIED -> ElectricCyan
                            ModuleStatus.ALERT -> StatusWarningYellow
                            else -> TextDim
                        },
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Right Status Badge
            when (status) {
                ModuleStatus.VERIFIED -> {
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .background(StatusSuccessGreen.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, StatusSuccessGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(7.dp)) {
                            val strokeWidth = 1.3.dp.toPx()
                            val path = Path().apply {
                                moveTo(size.width * 0.12f, size.height * 0.52f)
                                lineTo(size.width * 0.40f, size.height * 0.78f)
                                lineTo(size.width * 0.88f, size.height * 0.22f)
                            }
                            drawPath(
                                path = path,
                                color = StatusSuccessGreen,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }
                ModuleStatus.ALERT -> {
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .background(StatusWarningYellow.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, StatusWarningYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(7.dp)) {
                            val strokeWidth = 1.3.dp.toPx()
                            drawLine(
                                color = StatusWarningYellow,
                                start = Offset(size.width * 0.5f, size.height * 0.12f),
                                end = Offset(size.width * 0.5f, size.height * 0.55f),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = StatusWarningYellow,
                                radius = strokeWidth * 0.65f,
                                center = Offset(size.width * 0.5f, size.height * 0.85f)
                            )
                        }
                    }
                }
                ModuleStatus.IN_PROGRESS -> {
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .border(1.1.dp, ElectricCyan, CircleShape)
                    )
                }
                ModuleStatus.INACTIVE -> {
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .border(0.9.dp, TechBlue.copy(alpha = 0.4f), CircleShape)
                    )
                }
            }
        }
    }
}
