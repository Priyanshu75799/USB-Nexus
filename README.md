# USB Nexus 🔌

USB Nexus is an Android application built to detect, identify, and monitor connected USB devices and accessories (supporting both Micro-USB and USB Type-C ports).

I created this project to easily inspect connected USB hardware details and keep USB audio DACs active when the screen turns off.

## ✨ Features

- 🔍 **USB Device Identification:** Instantly scans connected USB peripherals (flash drives, keyboards, mice, audio DACs, controllers) and displays Vendor ID, Product ID, manufacturer, and class descriptors.
- ⚡ **Micro-USB & Type-C Support:** Supports both Micro-USB 5-pin and USB Type-C 24-pin ports.
- 🎵 **USB DAC Keep-Alive:** Helps keep connected USB DACs and headphone amplifiers active when the phone screen is locked or turned off (protection from DAC Suspension in some devices especially Micro-USB devices).
- 🌐 **Network State Monitor:** Displays active network status including USB Ethernet/LAN adapters.
- 📜 **USB History:** Automatically logs details of connected devices locally for quick reference.

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM
- **Storage:** DataStore Preferences & SharedPreferences
- **Min SDK:** 29 (Android 10) | **Target SDK:** 35 (Android 15)

## 🚀 Setup & Running

1. Clone this repository:
2. Open the project in Android Studio.
3. Connect an Android device with OTG support or use an emulator.
4. Click Run 'app' (Shift + F10) to build and install.

## 👨‍💻 Author

Created by **Priyanshu Rajput**  
Feel free to star ⭐ this repository if you find it helpful!

---

## 📖 Complete User Manual

Welcome to **USB Nexus**! This user manual explains how to use all the features of the app step by step.

---

### 1. 🚀 Startup & USB Port Selection
- **First Launch:** When you open USB Nexus for the first time, a **Device Configuration** dialog will pop up asking for your device's physical USB port type.
- **Micro-USB (5-Pin):** Select this option if your device uses a traditional Micro-USB connector.
- **Type-C (24-Pin):** Select this option if your device uses a modern USB Type-C connector.
- **PCB Trace Animation:** Once selected, the interactive hardware board visualizes 45-degree PCB trace routing animations from the connector pins to the central System-on-Chip (SoC).

---

### 2. 📱 Dashboard & USB Device Inspection
- **Universal Device Identification Area:** Displays your phone's model and active USB host hardware state.
- **Connecting a USB Device:** Attach any OTG peripheral (USB flash drive, mouse, keyboard, USB DAC, ethernet adapter, or game controller) using an OTG adapter/cable.
- **Real-Time Scanning:** The Dashboard automatically updates to display connected USB peripherals along with their **Vendor ID**, **Product ID**, **Device Class**, and **Manufacturer Name**.
- **Detailed Specifications Dialog:** Tap on any connected device card to open a full low-level specification report, including:
  - Device Path & Class Description
  - USB Interfaces & Endpoint Descriptors
  - Power & Bandwidth Configurations
  - Audio Sampling Rates & Resolution (for USB DACs)
- **Copy Specifications:** Tap the **Copy All Specifications to Clipboard** button inside the detail dialog to quickly export hardware details.

---

### 3. 🎵 USB DAC Keep-Alive Feature
- **What it does:** Some Android smartphones automatically suspend or cut power to connected USB Audio DACs / headphone amplifiers when the screen turns off or locks, causing audio stutters or disconnects.
- **How to Enable:**
  1. Open **Settings** or toggle the **DAC Anti-Disconnect / Keep-Alive** switch in the Dashboard.
  2. When active and a USB DAC is connected, USB Nexus streams an imperceptible 0dB silent audio signal directly down the USB DAC audio stream whenever the screen is turned off and no media is playing.
  3. This keeps the USB DAC power rail continuously active without consuming extra battery while playing music.

---

### 4. 🌐 Network & Ethernet Adapter State
- **USB Ethernet Monitoring:** Connect a USB-to-Ethernet (LAN) adapter to your phone via OTG.
- **Real-Time Status:** The Dashboard displays active network transport types (**Ethernet (LAN / USB Adapter)**, **Wi-Fi**, or **Cellular Mobile Data**).

---

### 5. 📜 Saved USB Device History
- **Automatic History Logging:** USB Nexus locally records details of all USB hardware ever connected to your device.
- **Accessing History:** Tap the **Saved History** button or navigate via Settings to view past USB connections.
- **Deleting Entries:** Tap the delete icon on any saved device entry to remove it from local history logs.

---

### 💡 Troubleshooting & Tips
- **Device Not Detected:** Ensure USB OTG is enabled in your Android phone's system settings (on some devices like Vivo, OPPO, OnePlus, OTG needs to be manually toggled ON in System Settings -> More Connections -> OTG).
- **USB Permission Prompt:** When connecting a new USB device, grant Android system USB permission when prompted so USB Nexus can read low-level hardware descriptors.