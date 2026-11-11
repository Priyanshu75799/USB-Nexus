package com.priyanshudeveloper.usbnexus.ui.startup.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priyanshudeveloper.usbnexus.phoenixengine.domain.model.UsbPortType
import com.priyanshudeveloper.usbnexus.ui.theme.CardBorder
import com.priyanshudeveloper.usbnexus.ui.theme.DarkSurface
import com.priyanshudeveloper.usbnexus.ui.theme.ElectricCyan
import com.priyanshudeveloper.usbnexus.ui.theme.TechBlue
import com.priyanshudeveloper.usbnexus.ui.theme.TextDim
import kotlin.math.abs

@Composable
fun AdvancedPcbCanvas(
    modifier: Modifier = Modifier,
    pinCount: Int = 5,
    portType: UsbPortType = UsbPortType.MICRO_USB,
    progress: Float = 0f,
    isActive: Boolean = false
) {
    val reusablePathMeasure = remember { PathMeasure() }
    val reusableSegmentPath = remember { Path() }
    val textMeasurer = rememberTextMeasurer()

    val pinLabelStyle = remember {
        TextStyle(
            color = ElectricCyan.copy(alpha = 0.85f),
            fontSize = 7.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }

    val silkScreenTextStyle = remember {
        TextStyle(
            color = TextDim.copy(alpha = 0.55f),
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { clip = false }
    ) {
        val w = size.width
        val h = size.height

        // Central SoC Chip Coordinates
        val socTop = 24.dp.toPx()
        val socBottom = (24 + 92).dp.toPx()
        val socCenterY = (socTop + socBottom) / 2f

        val socCenterX = w / 2f
        val socWidth = 92.dp.toPx()
        val socLeft = socCenterX - socWidth / 2f
        val socRight = socCenterX + socWidth / 2f

        // Reballing Motherboard Border Line Frame Anchors
        val socBorderMargin = 5.dp.toPx()
        val socBorderTop = socTop - socBorderMargin
        val socBorderBottom = socBottom + socBorderMargin
        val socBorderLeft = socLeft - socBorderMargin
        val socBorderRight = socRight + socBorderMargin

        // Bottom Charging Port Connector Y Level
        val bottomConnectorY = h - 45.dp.toPx()

        // Outer Card Edge Anchors (114dp width cards + 2dp start padding = 116dp exact right edge)
        val leftCardEdgeX = 116.dp.toPx()
        val rightCardEdgeX = w - 116.dp.toPx()

        val strokeBase = TechBlue.copy(alpha = if (isActive) 0.65f else 0.35f)
        val strokeGlow = ElectricCyan
        val nodeColor = if (isActive) ElectricCyan else TechBlue.copy(alpha = 0.6f)

        // =========================================================================
        // 0. PCB SILK-SCREEN GROUND GRID
        // =========================================================================
        drawPcbBackgroundGrid(
            w = w,
            socBorderBottom = socBorderBottom,
            bottomConnectorY = bottomConnectorY,
            gridColor = CardBorder.copy(alpha = 0.15f)
        )

        // =========================================================================
        // 1. MODULE CARDS PCB TRACES (All 6 Blocks Connect Directly to SoC)
        // =========================================================================

        val cardTraceWidth = 2.2.dp.toPx()

        // 1. Left Card 1 (5.0V Supply) -> Top-Left SoC Border
        draw45DegreePcbTrace(
            startX = leftCardEdgeX,
            startY = 18.dp.toPx(),
            endX = socBorderLeft,
            endY = socBorderTop + 14.dp.toPx(),
            baseColor = strokeBase,
            activeColor = strokeGlow,
            progress = progress,
            strokeW = cardTraceWidth,
            pathMeasure = reusablePathMeasure,
            segmentPath = reusableSegmentPath
        )

        // 2. Left Card 2 (Trust Security) -> Mid-Left SoC Border
        draw45DegreePcbTrace(
            startX = leftCardEdgeX,
            startY = 52.dp.toPx(),
            endX = socBorderLeft,
            endY = socCenterY,
            baseColor = strokeBase,
            activeColor = strokeGlow,
            progress = progress,
            strokeW = cardTraceWidth,
            pathMeasure = reusablePathMeasure,
            segmentPath = reusableSegmentPath
        )

        // 3. Left Card 3 (USB Hardware) -> Bottom-Left SoC Border
        draw45DegreePcbTrace(
            startX = leftCardEdgeX,
            startY = 88.dp.toPx(),
            endX = socBorderLeft,
            endY = socBorderBottom - 14.dp.toPx(),
            baseColor = strokeBase,
            activeColor = strokeGlow,
            progress = progress,
            strokeW = cardTraceWidth,
            pathMeasure = reusablePathMeasure,
            segmentPath = reusableSegmentPath
        )

        // 4. Right Card 1 (Detect USB Host) -> Top-Right SoC Border
        draw45DegreePcbTrace(
            startX = rightCardEdgeX,
            startY = 24.dp.toPx(),
            endX = socBorderRight,
            endY = socBorderTop + 14.dp.toPx(),
            baseColor = strokeBase,
            activeColor = strokeGlow,
            progress = progress,
            strokeW = cardTraceWidth,
            pathMeasure = reusablePathMeasure,
            segmentPath = reusableSegmentPath
        )

        // 5. Right Card 2 (Identify Device) -> Bottom-Right SoC Border
        draw45DegreePcbTrace(
            startX = rightCardEdgeX,
            startY = 110.dp.toPx(),
            endX = socBorderRight,
            endY = socBorderBottom - 14.dp.toPx(),
            baseColor = strokeBase,
            activeColor = strokeGlow,
            progress = progress,
            strokeW = cardTraceWidth,
            pathMeasure = reusablePathMeasure,
            segmentPath = reusableSegmentPath
        )

        // =========================================================================
        // 2. MAIN CONNECTOR BUS TRACES (45° CHAMFERED MOTHERBOARD ROUTING)
        // =========================================================================

        val isTypeC = portType == UsbPortType.TYPE_C || pinCount == 24

        if (!isTypeC) {
            // ---------------------------------------------------------------------
            // MICRO-USB (5-PIN PHYSICAL PCB TRACE BUS)
            // ---------------------------------------------------------------------
            val connectorPadWidth = 140.dp.toPx()
            val startX = socCenterX - connectorPadWidth / 2f
            val connectorStep = connectorPadWidth / 4f

            val socPinSpreadWidth = (socWidth + socBorderMargin * 2) * 0.76f
            val socStartX = socCenterX - socPinSpreadWidth / 2f
            val socStep = socPinSpreadWidth / 4f

            // Connector Housing Box
            drawRoundRect(
                color = DarkSurface,
                topLeft = Offset(startX - 12.dp.toPx(), bottomConnectorY - 4.dp.toPx()),
                size = Size(connectorPadWidth + 24.dp.toPx(), 22.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            drawRoundRect(
                color = TechBlue.copy(alpha = 0.6f),
                topLeft = Offset(startX - 12.dp.toPx(), bottomConnectorY - 4.dp.toPx()),
                size = Size(connectorPadWidth + 24.dp.toPx(), 22.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx()),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Silk-screen Text tag
            val tagResult = textMeasurer.measure("J1: MICRO-USB", style = silkScreenTextStyle)
            drawText(
                textLayoutResult = tagResult,
                topLeft = Offset(socCenterX - tagResult.size.width / 2f, bottomConnectorY + 20.dp.toPx())
            )

            val pinNames = arrayOf("5V", "D-", "D+", "ID", "GND")
            val traceW = 2.2.dp.toPx()

            for (i in 0 until 5) {
                val bX = startX + i * connectorStep
                val tX = socStartX + i * socStep

                // 1. SMD Connector Pad
                drawRoundRect(
                    color = if (progress > 0f) ElectricCyan else TechBlue,
                    topLeft = Offset(bX - 3.dp.toPx(), bottomConnectorY - 2.dp.toPx()),
                    size = Size(6.dp.toPx(), 10.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                // Pin Label Silk-screen
                val labelResult = textMeasurer.measure(pinNames[i], style = pinLabelStyle)
                drawText(
                    textLayoutResult = labelResult,
                    topLeft = Offset(bX - labelResult.size.width / 2f, bottomConnectorY + 9.dp.toPx())
                )

                // 2. 45-Degree Chamfered PCB Trace Bus Routing
                val dx = tX - bX
                val absDx = abs(dx)
                val yMid = (bottomConnectorY + socBorderBottom) / 2f
                val yBendStart = yMid + absDx / 2f
                val yBendEnd = yMid - absDx / 2f

                val tracePath = Path().apply {
                    moveTo(bX, bottomConnectorY)
                    lineTo(bX, yBendStart)
                    lineTo(tX, yBendEnd)
                    lineTo(tX, socBorderBottom)
                }

                drawPcbTrace(
                    path = tracePath,
                    baseColor = strokeBase,
                    activeColor = strokeGlow,
                    progress = progress,
                    strokeW = traceW,
                    pathMeasure = reusablePathMeasure,
                    segmentPath = reusableSegmentPath
                )

                // 3. Plated Through-Hole PCB Vias at 45° Direction Bends
                if (absDx > 1.dp.toPx()) {
                    drawPcbVia(center = Offset(bX, yBendStart), isActive = progress > 0.4f)
                    drawPcbVia(center = Offset(tX, yBendEnd), isActive = progress > 0.7f)
                }

                // 4. Gold SoC IC Pin Terminal Pad
                drawRoundRect(
                    color = if (progress > 0.85f) ElectricCyan else nodeColor,
                    topLeft = Offset(tX - 2.5.dp.toPx(), socBorderBottom - 4.dp.toPx()),
                    size = Size(5.dp.toPx(), 8.dp.toPx()),
                    cornerRadius = CornerRadius(1.5.dp.toPx())
                )
            }

        } else {
            // ---------------------------------------------------------------------
            // USB TYPE-C (24-PIN PROFESSIONAL PCB TRACE BUS ARRAY)
            // ---------------------------------------------------------------------
            val connectorPadWidth = (w * 0.62f).coerceIn(180.dp.toPx(), 260.dp.toPx())
            val startX = socCenterX - connectorPadWidth / 2f
            val connectorStep = connectorPadWidth / 23f

            val socPinSpreadWidth = socWidth + socBorderMargin * 2 - 8.dp.toPx()
            val socStartX = socCenterX - socPinSpreadWidth / 2f
            val socStep = socPinSpreadWidth / 23f

            // Metallic Type-C Connector Shield Frame
            drawRoundRect(
                color = DarkSurface,
                topLeft = Offset(startX - 10.dp.toPx(), bottomConnectorY - 6.dp.toPx()),
                size = Size(connectorPadWidth + 20.dp.toPx(), 26.dp.toPx()),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
            drawRoundRect(
                color = ElectricCyan.copy(alpha = 0.5f),
                topLeft = Offset(startX - 10.dp.toPx(), bottomConnectorY - 6.dp.toPx()),
                size = Size(connectorPadWidth + 20.dp.toPx(), 26.dp.toPx()),
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Silk-screen Text Tag
            val tagResult = textMeasurer.measure("J1: TYPE-C", style = silkScreenTextStyle)
            drawText(
                textLayoutResult = tagResult,
                topLeft = Offset(socCenterX - tagResult.size.width / 2f, bottomConnectorY + 22.dp.toPx())
            )

            val traceW = 1.2.dp.toPx()

            for (i in 0 until 24) {
                val isUpperRow = i % 2 == 0
                val bX = startX + i * connectorStep
                val tX = socStartX + i * socStep
                val padY = if (isUpperRow) bottomConnectorY - 2.dp.toPx() else bottomConnectorY + 8.dp.toPx()

                // 1. Dual-Row Staggered SMD Connector Pads
                drawCircle(
                    color = if (progress > 0f) ElectricCyan else TechBlue,
                    radius = 2.0.dp.toPx(),
                    center = Offset(bX, padY)
                )

                // 2. 45-Degree Chamfered PCB Trace Bus Routing
                val dx = tX - bX
                val absDx = abs(dx)

                // Stagger bend zone by signal groups to prevent visual overlap
                val groupOffset = ((i / 6) * 6.dp.toPx()) - 9.dp.toPx()
                val yMid = (bottomConnectorY + socBorderBottom) / 2f + groupOffset
                val yBendStart = yMid + absDx / 2f
                val yBendEnd = yMid - absDx / 2f

                val tracePath = Path().apply {
                    moveTo(bX, padY)
                    lineTo(bX, yBendStart)
                    lineTo(tX, yBendEnd)
                    lineTo(tX, socBorderBottom)
                }

                drawPcbTrace(
                    path = tracePath,
                    baseColor = strokeBase,
                    activeColor = strokeGlow,
                    progress = progress,
                    strokeW = traceW,
                    pathMeasure = reusablePathMeasure,
                    segmentPath = reusableSegmentPath
                )

                // 3. Plated Through-Hole PCB Vias at 45° Direction Changes
                if (absDx > 2.dp.toPx()) {
                    drawPcbVia(
                        center = Offset(bX, yBendStart),
                        radius = 1.6.dp.toPx(),
                        isActive = progress > 0.4f
                    )
                    drawPcbVia(
                        center = Offset(tX, yBendEnd),
                        radius = 1.6.dp.toPx(),
                        isActive = progress > 0.75f
                    )
                }

                // 4. Gold SoC Pin Terminal Pad
                drawCircle(
                    color = if (progress > 0.85f) ElectricCyan else nodeColor,
                    radius = 1.8.dp.toPx(),
                    center = Offset(tX, socBorderBottom)
                )
            }
        }
    }
}

// Draw 45-degree angle connected side card traces
private fun DrawScope.draw45DegreePcbTrace(
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    baseColor: Color,
    activeColor: Color,
    progress: Float,
    strokeW: Float,
    pathMeasure: PathMeasure,
    segmentPath: Path
) {
    val dx = endX - startX

    val path = Path().apply {
        moveTo(startX, startY)
        val midX = startX + dx * 0.45f
        lineTo(midX, startY)
        lineTo(endX, endY)
    }

    drawPcbTrace(
        path = path,
        baseColor = baseColor,
        activeColor = activeColor,
        progress = progress,
        strokeW = strokeW,
        pathMeasure = pathMeasure,
        segmentPath = segmentPath
    )

    // Terminal Pin Pad on Card Edge
    drawCircle(
        color = if (progress > 0f) ElectricCyan else TechBlue,
        radius = 3.2.dp.toPx(),
        center = Offset(startX, startY)
    )

    // Inner Terminal Dot
    drawCircle(
        color = ElectricCyan,
        radius = 1.2.dp.toPx(),
        center = Offset(startX, startY)
    )

    // Terminal Pin Pad on SoC Border
    drawCircle(
        color = if (progress > 0.8f) ElectricCyan else TechBlue,
        radius = 3.0.dp.toPx(),
        center = Offset(endX, endY)
    )
}

// Draw PCB Plated Via (Outer metallic ring + inner hole)
private fun DrawScope.drawPcbVia(
    center: Offset,
    radius: Float = 2.4.dp.toPx(),
    isActive: Boolean = false
) {
    drawCircle(
        color = if (isActive) ElectricCyan else TechBlue.copy(alpha = 0.6f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.0.dp.toPx())
    )
    drawCircle(
        color = Color(0xFF030712),
        radius = radius * 0.45f,
        center = center
    )
}

// Draw Background PCB Copper Hatch / Grid
private fun DrawScope.drawPcbBackgroundGrid(
    w: Float,
    socBorderBottom: Float,
    bottomConnectorY: Float,
    gridColor: Color
) {
    val step = 32.dp.toPx()
    var x = step
    while (x < w) {
        drawLine(
            color = gridColor,
            start = Offset(x, socBorderBottom + 10.dp.toPx()),
            end = Offset(x, bottomConnectorY - 10.dp.toPx()),
            strokeWidth = 0.6.dp.toPx()
        )
        x += step
    }

    val yStart = socBorderBottom + 20.dp.toPx()
    val yEnd = bottomConnectorY - 20.dp.toPx()
    var y = yStart
    while (y < yEnd) {
        drawLine(
            color = gridColor,
            start = Offset(20.dp.toPx(), y),
            end = Offset(w - 20.dp.toPx(), y),
            strokeWidth = 0.6.dp.toPx()
        )
        y += step
    }
}

private fun DrawScope.drawPcbTrace(
    path: Path,
    baseColor: Color,
    activeColor: Color,
    progress: Float,
    strokeW: Float,
    pathMeasure: PathMeasure,
    segmentPath: Path
) {
    drawPath(
        path = path,
        color = baseColor,
        style = Stroke(width = strokeW)
    )

    if (progress > 0f) {
        pathMeasure.setPath(path, false)
        val length = pathMeasure.length
        val currentLen = length * progress.coerceIn(0f, 1f)

        if (currentLen > 0f) {
            segmentPath.reset()
            pathMeasure.getSegment(0f, currentLen, segmentPath, true)

            drawPath(
                path = segmentPath,
                color = activeColor,
                style = Stroke(width = strokeW + 0.8.dp.toPx())
            )

            val headPos = pathMeasure.getPosition(currentLen)
            drawCircle(
                color = ElectricCyan,
                radius = strokeW * 1.5f,
                center = headPos
            )
            drawCircle(
                color = Color.White,
                radius = strokeW * 0.7f,
                center = headPos
            )
        }
    }
}
