package com.priyanshudeveloper.usbnexus.ui.startup.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priyanshudeveloper.usbnexus.ui.theme.ElectricCyan
import com.priyanshudeveloper.usbnexus.ui.theme.TechBlue
import com.priyanshudeveloper.usbnexus.ui.theme.TextPrimary
import com.priyanshudeveloper.usbnexus.ui.theme.TextSecondary

@Composable
fun SocChipView(
    modifier: Modifier = Modifier,
    chipSize: Dp = 92.dp,
    isActive: Boolean = false,
    label: String = "SoC",
    subLabel: String = "USB NEXUS"
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) ElectricCyan else TechBlue.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "chipBorderColor"
    )

    val chipBgColor = Color(0xFB071326)

    Box(
        modifier = modifier
            .size(chipSize + 28.dp)
            .graphicsLayer {
                clip = false
            },
        contentAlignment = Alignment.Center
    ) {
        // Motherboard Reballing / Mounting Border Line Canvas (Replaces individual pins)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val chipPx = chipSize.toPx()
            val left = (w - chipPx) / 2f
            val top = (h - chipPx) / 2f

            val borderMargin = 5.dp.toPx()
            val outerLeft = left - borderMargin
            val outerTop = top - borderMargin
            val outerWidth = chipPx + (borderMargin * 2)
            val outerHeight = chipPx + (borderMargin * 2)

            // Outer Motherboard Mounting BGA / Reballing Border Line
            drawRoundRect(
                color = if (isActive) ElectricCyan else TechBlue.copy(alpha = 0.7f),
                topLeft = Offset(outerLeft, outerTop),
                size = Size(outerWidth, outerHeight),
                cornerRadius = CornerRadius(12.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner Accent Solder Ring Border
            drawRoundRect(
                color = TechBlue.copy(alpha = 0.35f),
                topLeft = Offset(left - 1.5.dp.toPx(), top - 1.5.dp.toPx()),
                size = Size(chipPx + 3.dp.toPx(), chipPx + 3.dp.toPx()),
                cornerRadius = CornerRadius(11.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // Corner Reballing Solder Anchor Pads
            val padRadius = 2.5.dp.toPx()
            val padColor = if (isActive) ElectricCyan else TechBlue
            drawCircle(color = padColor, radius = padRadius, center = Offset(outerLeft, outerTop))
            drawCircle(color = padColor, radius = padRadius, center = Offset(outerLeft + outerWidth, outerTop))
            drawCircle(color = padColor, radius = padRadius, center = Offset(outerLeft, outerTop + outerHeight))
            drawCircle(color = padColor, radius = padRadius, center = Offset(outerLeft + outerWidth, outerTop + outerHeight))

            // Outer Glowing Aura when active
            if (isActive) {
                drawRoundRect(
                    color = ElectricCyan.copy(alpha = 0.35f),
                    topLeft = Offset(outerLeft - 3.dp.toPx(), outerTop - 3.dp.toPx()),
                    size = Size(outerWidth + 6.dp.toPx(), outerHeight + 6.dp.toPx()),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Inner Chip Main Body
        Box(
            modifier = Modifier
                .size(chipSize)
                .background(chipBgColor, shape = RoundedCornerShape(10.dp))
                .border(2.dp, borderColor, shape = RoundedCornerShape(10.dp))
                .border(1.dp, TechBlue.copy(alpha = 0.3f), shape = RoundedCornerShape(10.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = subLabel,
                    color = if (isActive) ElectricCyan else TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}
