USB Nexus 🔌

USB Connectivity & Audio Adapter Management

USB Nexus is an Android application designed primarily to help users connect, manage, identify, and monitor USB devices and accessories on compatible Micro-USB and USB Type-C Android smartphones.

The application focuses especially on USB Type-C to 3.5 mm audio adapters, allowing users with compatible Micro-USB OTG setups to use supported Type-C audio converters.

USB Nexus also includes an intelligent USB Audio Keep-Alive mechanism designed to help prevent USB DAC suspension on compatible Android devices.

✨ Features

🎵 1. Intelligent USB Audio Keep-Alive

- Helps prevent supported USB audio devices from being suspended by the Android Linux kernel.
- Continuously monitors media playback activity, including audio and video playback.
- Automatically disables Keep-Alive when active media playback is detected.
- Automatically activates when no media playback is detected.
- Sends a silent audio signal to the connected USB DAC to maintain an active audio stream.
- Particularly useful for certain Micro-USB devices that experience USB audio suspension.

🔌 2. Micro-USB & USB Type-C Compatibility

- Supports compatible Android devices with Micro-USB and USB Type-C connectors.
- Allows compatible Micro-USB phones to use supported USB Type-C to 3.5 mm audio adapters through an appropriate Type-C to Micro-USB OTG adapter.
- Designed to work with different compatible USB audio converters.
- Helps users use external DAC-based audio output instead of relying on their phone's built-in 3.5 mm audio jack.

🔍 3. USB Device Identification

- Detects and identifies connected USB peripherals.
- Displays Vendor ID (VID), Product ID (PID), manufacturer information and device class descriptors.
- Supports inspection of compatible USB peripherals, including:
  - USB Flash Drives
  - Keyboards
  - Mice
  - USB Audio DACs
  - Game Controllers
  - Ethernet Adapters

🌐 4. Network State Monitor

- Displays active network connection status.
- Monitors available network transport types, including Wi-Fi, Cellular Data and Ethernet.
- Supports network status visibility when using compatible USB Ethernet adapters.

📜 5. USB Connection History

- Maintains a local history of connected USB devices.
- Allows users to review previously connected peripherals.
- Provides an option to remove individual entries from saved history.

⚙️ 6. Device-Specific Configuration

- On the first launch, a Device Configuration dialog asks users to select their phone's physical USB connector type.
- Separate Micro-USB and USB Type-C modules are activated according to the selected configuration.
- The startup animation is also configured according to the selected connector type.
- Includes an interactive PCB-inspired animation with connector-to-SoC trace routing.

🧩 7. Phoenix Engine

USB Nexus uses Phoenix Engine, a dedicated modular backend architecture developed for the application.

- Organizes backend components into a structured folder architecture.
- Separates Micro-USB and USB Type-C handling modules.
- Provides a modular foundation for USB detection, management and monitoring.
- Designed with lightweight and organized backend execution in mind.

🛠️ Technology & Architecture

- Programming Language: Kotlin
- Frontend: Jetpack Compose and XML-based layouts
- Backend Architecture: Phoenix Engine
- Application Architecture: Modular backend design
- Minimum Android Version: Android 10 (API 29)
- Target SDK: 35 (Android 15)

🔐 Lightweight & Privacy-Conscious Design

- Designed for a small background memory footprint.
- Does not request storage permission.
- Does not request overlay permission.
- Avoids unnecessary access to storage and screen overlays.
- Designed to perform its USB-related tasks without requiring unnecessary permissions.

🚀 Setup & Running

1. Clone or download this repository.
2. Open the project in Android Studio.
3. Connect a compatible Android device with USB OTG support.
4. Build and install the application using Android Studio.

📖 Complete User Manual

1. First Launch & USB Port Selection

When USB Nexus is launched for the first time:

1. A Device Configuration dialog appears.
2. Select Micro-USB if your phone uses a Micro-USB connector.
3. Select USB Type-C if your phone uses a USB Type-C connector.
4. The application activates the corresponding device-specific modules and startup animation.

2. Dashboard & USB Device Inspection

- Open the Dashboard to view your device information and USB connection status.
- Connect a USB peripheral using a compatible OTG adapter or cable.
- USB Nexus detects and displays supported connected peripherals.
- Select a device card to inspect its available hardware information.

Depending on the connected device, available information may include:

- Vendor ID and Product ID
- Device Path and Class
- Manufacturer Information
- USB Interfaces and Endpoint Descriptors
- Power and Bandwidth Configurations
- Audio Sampling Rates and Resolution (for supported USB audio devices)

3. USB Audio Keep-Alive

The Keep-Alive mechanism is designed to maintain an active USB audio stream when no media is playing.

How it works:

1. USB Nexus monitors media playback activity.
2. When audio or video playback is detected, Keep-Alive automatically becomes inactive.
3. When media playback stops, Keep-Alive automatically becomes active.
4. A silent audio signal is sent to the connected USB DAC to help maintain its active audio stream.

This mechanism is particularly useful on compatible devices that are prone to USB audio suspension.

4. Network & Ethernet Monitoring

- Connect a compatible USB Ethernet adapter through OTG.
- Open the Network State Monitor.
- View the available network transport status, including Ethernet, Wi-Fi and Cellular Data.

5. Saved USB Device History

- Open Saved History to review previously connected USB devices.
- View locally saved device information.
- Use the delete option to remove individual history entries.

⚠️ Compatibility & Important Notes

- Requires Android 10 or later.
- Includes compatibility work for newer Android versions, including Android 14 APIs.
- Requires compatible USB Host/OTG hardware.
- Appropriate OTG adapters and compatible USB peripherals are necessary.
- Not every USB Type-C audio adapter is guaranteed to work with every Micro-USB smartphone.
- USB audio functionality depends on the device's hardware, Android implementation, Linux kernel behaviour and connected adapter.
- USB Nexus cannot add physical USB Host or OTG capabilities to unsupported devices.
Background Execution & Battery Optimization

USB Audio Keep-Alive relies on background execution to maintain an active USB audio stream.

On Android devices with aggressive background process management, users may need to manually configure the application's battery settings.

Recommended configuration:

- Open Android Settings.
- Navigate to Apps → USB Nexus → Battery.
- Select Unrestricted (if available).

This helps reduce the likelihood of the operating system terminating USB Nexus during background operation.

The exact settings and menu names may vary depending on the Android manufacturer and OS version.
📸 Application Screenshots

Explore the USB Nexus interface and its key features.

 Dashboard

<img width="720" height="1600" alt="Dashboard" src="https://github.com/user-attachments/assets/b42691ef-d6ad-468d-a5ec-56b9afbf5c8d" />


 Saved USB Device

 <img width="720" height="1600" alt="Saved_USB_device" src="https://github.com/user-attachments/assets/938d7f1f-8f3b-4422-80a0-8ee2cfc0162f" />


 USB Connectivity

<img width="720" height="1600" alt="dashboard_with _connected_usb_device" src="https://github.com/user-attachments/assets/ec94c442-4401-4510-b568-f29fc328fe3a" />

 Settings

 <img width="720" height="1600" alt="Settings_section" src="https://github.com/user-attachments/assets/9dba2af2-872a-4439-8bfb-aa1a4a6faa19" />

---

👨‍💻 Author

Priyanshu Rajput

Developer of USB Nexus and the Phoenix Engine architecture.

USB Nexus | Connect. Manage. Monitor. 🔌

If you find this project useful, consider giving the repository a ⭐ star!
