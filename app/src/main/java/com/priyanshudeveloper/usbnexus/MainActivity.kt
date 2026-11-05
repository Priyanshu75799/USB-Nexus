package com.priyanshudeveloper.usbnexus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.priyanshudeveloper.usbnexus.phoenixengine.usb.UsbHardwareCommunicator
import com.priyanshudeveloper.usbnexus.phoenixengine.viewmodel.StartupViewModel
import com.priyanshudeveloper.usbnexus.ui.navigation.UsbNexusNavigation
import com.priyanshudeveloper.usbnexus.ui.theme.DarkBackground
import com.priyanshudeveloper.usbnexus.ui.theme.USBNexusTheme

class MainActivity : ComponentActivity() {

    private val startupViewModel: StartupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingUsbIntent(intent)
        setContent {
            USBNexusTheme {
                UsbNexusApp(viewModel = startupViewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingUsbIntent(intent)
    }

    private fun handleIncomingUsbIntent(intent: android.content.Intent?) {
        if (intent == null) return
        val communicator = UsbHardwareCommunicator(this)
        communicator.handleUsbIntent(intent)
    }
}

@Composable
fun UsbNexusApp(viewModel: StartupViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .displayCutoutPadding(),
            color = DarkBackground
        ) {
            UsbNexusNavigation(viewModel = viewModel)
        }
    }
}
