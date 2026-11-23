# USB Nexus 

USB Nexus is an Android application built to detect, identify, and monitor connected USB devices and accessories (supporting both Micro-USB and USB Type-C ports).

I created this project to easily inspect connected USB hardware details and keep USB audio DACs active when the screen turns off.

---

##  What's New in Version 1.7 (Release Notes)

###  Fixes & Stability Improvements:
1. **DAC Anti-Suspend System Stability:** Fixed background bugs in the DAC Anti-Suspend service so it starts smoothly without any app crashes or service hangs.
2. **Notification Reliability & Direct Action:** Improved background connection notifications so users can activate the DAC Anti-Suspend service directly from the notification shade without crashes.
3. **Enhanced Micro-USB & Type-C DAC Support (Android 10+):** Micro-USB devices running Android 10 and above with OTG support can now seamlessly use USB Type-C to 3.5mm DAC dongles (built-in DAC chips) without facing app crashes (requires granting requested system permissions).

###  New Features & Additions:
1. **Developer Details & Automatic GitHub Release Detection Block:** Added a dedicated Developer & GitHub Channel section inside Settings. It dynamically displays the current app version (`v1.7`), developer information (Priyanshu Rajput), and contact Email ID. Whenever a new release is published on GitHub, the app automatically detects it and activates a cylindrical download button linking directly to the latest release page.
2. **Automatic 24-Hour Release Update Notifications:** When a new version is published on GitHub, older versions of the app will automatically send a high-priority update notification every 24 hours until updated. Tapping the notification opens the latest GitHub release download page directly.

---

##  Features

-  **USB Device Identification:** Instantly scans connected USB peripherals (flash drives, keyboards, mice, audio DACs, controllers) and displays Vendor ID, Product ID, manufacturer, and class descriptors.
-  **Micro-USB & Type-C Support:** Supports both Micro-USB 5-pin and USB Type-C 24-pin ports.
-  **USB DAC Keep-Alive:** Helps keep connected USB DACs and headphone amplifiers active when the phone screen is locked or turned off (protection from DAC Suspension in some devices especially Micro-USB devices).
-  **Network State Monitor:** Displays active network status including USB Ethernet/LAN adapters.
-  **Instant Background Alerts:** Sends real-time pop-up notifications when USB hardware is connected, even if the app is closed or the screen is locked.
-  **Automatic Update Alerts:** Checks GitHub for new releases and sends smart notifications every 24 hours whenever a new app version is available.
-  **USB History:** Automatically logs details of connected devices locally for quick reference.

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM
- **Storage:** DataStore Preferences & SharedPreferences
- **Min SDK:** 29 (Android 10) | **Target SDK:** 35 (Android 15)

##  Setup & Running

1. Clone this repository:
   ```bash
   git clone https://github.com/Priyanshu75799/USB-Nexus.git
   ```
2. Open the project in Android Studio.
3. Connect an Android device with OTG support or use an emulator.
4. Click Run 'app' (Shift + F10) to build and install.

## 👨‍💻 Author

Created by **Priyanshu Rajput**  
GitHub: [Priyanshu75799/USB-Nexus](https://github.com/Priyanshu75799/USB-Nexus)  
Email: `iampriyanshudeveloper@gmail.com`  
Feel free to star ⭐ this repository if you find it helpful!

---

##  Complete User Manual

Welcome to **USB Nexus**! This user manual explains how to use all the features of the app step by step.

---

### 1.  Startup & USB Port Selection
- **First Launch:** When you open USB Nexus for the first time, a **Device Configuration** dialog will pop up asking for your device's physical USB port type.
- **Micro-USB (5-Pin):** Select this option if your device uses a traditional Micro-USB connector.
- **Type-C (24-Pin):** Select this option if your device uses a modern USB Type-C connector.
- **PCB Trace Animation:** Once selected, the interactive hardware board visualizes 45-degree PCB trace routing animations from the connector pins to the central System-on-Chip (SoC).

---

### 2.  Dashboard & USB Device Inspection
- **Universal Device Identification Area:** Displays your phone's model and active USB host hardware state.
- **Permission Status Capsules:** The Dashboard includes interactive permission status capsule buttons (for **Battery Optimization** and **Notification Permissions**). If a permission status shows "Required" (in red), tap the capsule button to quickly grant the necessary system permissions so USB Nexus can detect hardware in the background without being restricted by system battery savers.
- **Connecting a USB Device:** Attach any OTG peripheral (USB flash drive, mouse, keyboard, USB DAC, ethernet adapter, or game controller) using an OTG adapter/cable.
- **Real-Time Scanning:** The Dashboard automatically updates to display connected USB peripherals along with their **Vendor ID**, **Product ID**, **Device Class**, and **Manufacturer Name**.
- **Detailed Specifications Dialog:** Tap on any connected device card to open a full low-level specification report, including:
  - Device Path & Class Description
  - USB Interfaces & Endpoint Descriptors
  - Power & Bandwidth Configurations
  - Audio Sampling Rates & Resolution (for USB DACs)
- **Copy Specifications:** Tap the **Copy All Specifications to Clipboard** button inside the detail dialog to quickly export hardware details.

---

### 3.  USB DAC Keep-Alive Feature
- **What it does:** Some Android smartphones automatically suspend or cut power to connected USB Audio DACs / headphone amplifiers when the screen turns off or locks, causing audio stutters or disconnects.
- **How to Enable:**
  1. Open **Settings** or toggle the **DAC Anti-Suspend / Keep-Alive** switch in the Dashboard.
  2. When active and a USB DAC is connected, USB Nexus streams an imperceptible 0dB silent audio signal directly down the USB DAC audio stream whenever the screen is turned off and no media is playing.
  3. This keeps the USB DAC power rail continuously active without consuming extra battery while playing music.

---

### 4.  Network & Ethernet Adapter State
- **USB Ethernet Monitoring:** Connect a USB-to-Ethernet (LAN) adapter to your phone via OTG.
- **Real-Time Status:** The Dashboard displays active network transport types (**Ethernet (LAN / USB Adapter)**, **Wi-Fi**, or **Cellular Mobile Data**).

---

### 5.  Saved USB Device History
- **Automatic History Logging:** USB Nexus locally records details of all USB hardware ever connected to your device.
- **Accessing History:** Tap the **Saved History** button or navigate via Settings to view past USB connections.
- **Deleting Entries:** Tap the delete icon on any saved device entry to remove it from local history logs.

---

### 6.  Instant Background Connection Alerts & Update Notifications
- **Background Connection Alerts:** USB Nexus automatically detects connected USB hardware and sends high-priority notifications even if the app is closed or running in the background.
- **Quick Action inside Notification:** Tap the **Enable DAC Anti-Suspend** action directly inside the connection notification to activate DAC keep-alive in one tap.
- **Automatic Release Update Alerts:** Automatically sends system notifications every 24 hours when a new GitHub release is available, with a single tap to open the latest release download page.

---

###  Troubleshooting & Tips
- **Device Not Detected:** Ensure USB OTG is enabled in your Android phone's system settings (on some devices like Vivo, OPPO, OnePlus, OTG needs to be manually toggled ON in System Settings -> More Connections -> OTG).
- **USB Permission Prompt:** When connecting a new USB device, grant Android system USB permission when prompted so USB Nexus can read low-level hardware descriptors.
- **Notification Permission:** On Android 13+ (API 33+), allow notification permission so instant connection alerts can pop up in real time.
