package com.priyanshudeveloper.usbnexus.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.priyanshudeveloper.usbnexus.R
import com.priyanshudeveloper.usbnexus.ui.theme.TechBlue
import com.priyanshudeveloper.usbnexus.ui.theme.TextPrimary
import com.priyanshudeveloper.usbnexus.ui.theme.TextSecondary

@Composable
fun UsbNexusLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showBorderFrame: Boolean = true,
    showText: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = if (showBorderFrame) {
                Modifier
                    .border(
                        width = 1.5.dp,
                        color = TechBlue.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            } else {
                Modifier
            },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.usbimage),
                contentDescription = "USB Nexus Trident Symbol",
                modifier = Modifier.size(size)
            )
        }

        if (showText) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "USB Nexus",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "USB Hardware Identifier & Security Monitor Engine",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
        }
    }
}
