# 🚗 RoadSync Automotive - VHAL Demo App & Digital Cluster Dashboard

A modern, production-grade **Android Automotive OS (AAOS)** Digital Instrument Cluster Dashboard application built with **Jetpack Compose**, **Hilt**, **Coroutines / StateFlow**, and the **Android Vehicle Hardware Abstraction Layer (VHAL)** API.

---

## 📸 Digital Instrument Cluster Overview

The app features a futuristic, high-end EV automotive cockpit display with real-time VHAL telemetry integration, interactive controls, and visual effects:

```text
 ┌──────────────────────────────────────────────────────────────────────────────────────────────────────────┐
 │ ᛒ Pixel 9 Pro... (18%)   🕶️  ⛔    │ [✉ 4] [👤 2] [01:48 📞] ⚠️ ((•)) A  ⬡  │ Fri 24 Oct 10:22 PM  LTE 📶 │
 ├──────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │                                      ┌────────────────────────────┐                                      │
 │  ┌──────────┐                        │ ── SONIC ⇄    62 km Range ─│                                      │
 │  │ P        │                        └────────────────────────────┘                                      │
 │  │ A        │                                                                     ⚡⚡ Rapid Brake        │
 │  │ R        │                                  8 8                                (( SUPER Hold ))       │
 │  │ K        │                                k m / h                              🔴 80 Speed Exceeded   │
 │  │ I        │                          /───────────────────\                      ⏲️ Active 72 km/h     │
 │  │ N        │                         /   |||||||||||||||   \                                            │
 │  │ G        │                         \_____________________/                                            │
 │  │ »        │                             < ODO 1324 km >                                                │
 ├──────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │               [⊞]  [▲]  [💡]  [⬐]  [⬅]  [🔧]  [(TC)]  [➡]  [⚙️]  [🔋 100%]  [⚙️ ON]                        │
 └──────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Architecture & Engineering Approach

The project strictly adheres to **Clean Architecture**, **MVVM**, and **Unidirectional Data Flow (UDF)**:

```text
┌─────────────────────────────────────────────────────────┐
│              Vehicle Hardware / VHAL Sensors             │
└────────────────────────────┬────────────────────────────┘
                             │ (CarPropertyEventCallback)
                             ▼
┌─────────────────────────────────────────────────────────┐
│               VehiclePropertyRepository                 │
│  - Registers VHAL callbacks                             │
│  - Converts units (m/s -> km/h)                         │
│  - Manages UI state defaults & live updates             │
└────────────────────────────┬────────────────────────────┘
                             │ updates
                             ▼
┌─────────────────────────────────────────────────────────┐
│            MutableStateFlow<VehicleUiState>             │
│            StateFlow<VehicleUiState> (Read-Only)        │
└────────────────────────────┬────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────┐
│                     VhalViewModel                       │
│  - Bridges repository state to UI                       │
│  - Exposes turn signal, mode, & power controls          │
└────────────────────────────┬────────────────────────────┘
                             │ collectAsStateWithLifecycle()
                             ▼
┌─────────────────────────────────────────────────────────┐
│                 Jetpack Compose UI                      │
│  - Futuristic Cockpit Cluster & Bottom Control Dock     │
└────────────────────────────┴────────────────────────────┘
```

### Architectural Principles:
1. **Separation of Concerns:** Hardware interactions are isolated inside `VehiclePropertyRepository`, leaving UI components completely decoupled from VHAL platform internals.
2. **Dependency Injection (Hilt):** Singleton scopes for repository and service instances, `@HiltViewModel` for ViewModel lifecycle management.
3. **Asynchronous Reactive Pipeline:** Uses Kotlin Coroutines (`Dispatchers.IO` for VHAL hardware calls) and `StateFlow` for state propagation.
4. **Declarative UI & Custom Graphics:** Uses Jetpack Compose with custom `Canvas` rendering for the 3D cockpit deck (`21`, `23`, `24`, `25`), glowing neon borders, and infinite transition turn signal animations (`⬅` / `➡`).

---

## 🛠️ Build & Run Instructions

### Prerequisites
- **Android Studio:** Jellyfish / Koala / Ladybug or newer.
- **JDK:** Version 17.
- **Android SDK:** SDK 35 (`compileSdk = 35`, `minSdk = 31`).
- **Target Device:** Android Automotive OS Emulator (e.g. `Automotive (1024p landscape) API 34/35`) or AAOS Hardware Target.

---

### Step 1: Clone & Build Locally
```bash
# Clone the repository
git clone https://github.com/your-org/RoadSync-Automotive-Demo-App.git
cd RoadSync-Automotive-Demo-App

# Build Debug APK
./gradlew assembleDebug
```

---

### Step 2: Running on Emulator / Target Device

#### Option A: Fast Automated System Deployment (`push_priv_app.ps1`)
For full privileged VHAL access on a rootable AAOS emulator:
```powershell
powershell -ExecutionPolicy Bypass -File .\push_priv_app.ps1
```
*(Handles `adb root`, `adb remount`, pushing to `/system/priv-app/MyAutomotiveApp/`, setting permissions, and launching `MainActivity` under User 10).*

#### Option B: Quick Daily UI Iteration (`adb install -r`)
Once installed in `/system/priv-app/`, fast-deploy UI updates without rebuilding system images:
```powershell
./gradlew assembleDebug
adb install -r --user 10 app/build/outputs/apk/debug/app-debug.apk
adb shell am force-stop com.example.vhaldemoapp
adb shell am start --user 10 -n com.example.vhaldemoapp/.MainActivity
```

#### Option C: Production AOSP System Image Baking (`aosp_integration/`)
To bake directly into AOSP `system.img`:
1. Copy `aosp_integration/` to `device/generic/automotive/vhaldemoapp/` in your AOSP tree.
2. Add `PRODUCT_PACKAGES += VHALDemoApp` in `device.mk`.
3. Build AOSP image:
   ```bash
   source build/envsetup.sh
   lunch sdk_car_x86_64-userdebug
   m -j$(nproc)
   ```

---

## ⚠️ Assumptions & Limitations

1. **Android Automotive OS Platform Environment:**
   - **Assumption:** Accessing VHAL properties relies on `android.car.jar` (included in `libs/android.car.jar` for local compilation).
   - **Limitation:** On standard non-Automotive phones/tablets, Android Car Service is unavailable. The app detects this gracefully and operates in **Standalone Demo Mode** with realistic default telemetry (`88 km/h`, `100% Battery`, `SONIC Mode`, `62 km Range`, `1324 km ODO`).

2. **Privileged System Permissions:**
   - **Assumption:** Accessing sensitive VHAL properties (`CAR_SPEED`, `CAR_ENERGY`, `CONTROL_CAR_CLIMATE`) requires the app to reside in `/system/priv-app/` with permissions whitelisted via `privapp-permissions.xml`.
   - **Limitation:** Installing as a standard user app (`adb install`) without system privilege placement will block hardware property callbacks from VHAL.

3. **VHAL Hardware Property Support:**
   - **Limitation:** Property change callbacks depend on hardware HAL capability. Properties not supported by a given vehicle ECU will fallback to default UI state.

---

## 🧪 Test Execution Steps

### 1. Running Unit Tests
To execute all local JVM unit tests:
```bash
./gradlew test
# Or specific debug unit test task:
./gradlew app:testDebugUnitTest
```

---

### 2. Running Instrumented UI Tests
Ensure an Android Automotive Emulator or connected target device is running:
```bash
./gradlew connectedAndroidTest
# Or specific debug instrumented test task:
./gradlew app:connectedDebugAndroidTest
```

---

### 3. VHAL Hardware Signal Injection Testing (ADB)

Simulate live vehicle hardware signals on the emulator using ADB commands:

```powershell
# 1. Inject Vehicle Speed = 88 km/h (24.44 m/s)
adb shell cmd car_service inject-continuous-events 0x11600207 24.44

# 2. Inject Vehicle Speed = 0 km/h
adb shell cmd car_service inject-continuous-events 0x11600207 0.0

# 3. Inject EV Battery Level = 100%
adb shell cmd car_service inject-vhal-event EV_BATTERY_LEVEL 0 100.0

# 4. Change Gear to DRIVE (Gear ID = 8)
adb shell cmd car_service inject-vhal-event GEAR_SELECTION 0 8

# 5. Change Gear to PARK (Gear ID = 4)
adb shell cmd car_service inject-vhal-event GEAR_SELECTION 0 4

# 6. Read Live Speed Property Value
adb shell cmd car_service get-property-value PERF_VEHICLE_SPEED
```

---

### 4. Manual Verification Steps
1. **Turn Signals:** Tap left (`⬅`) or right (`➡`) arrow buttons in the bottom dock to verify green pulsing indicator animations.
2. **High Beam:** Tap high beam button (`💡`) to toggle blue illumination state.
3. **Power System:** Tap `⚙️ ON` pill to toggle HVAC power state.
4. **Parking Drawer:** Tap `PARKING >>` vertical left tab to toggle active parking mode state.
5. **Speed Animation:** Inject VHAL speed changes via ADB and observe smooth digit stepping without jump artifacts.

---

## 📁 Project Structure

```text
RoadSync-Automotive-Demo-App/
├── app/src/main/java/com/example/vhaldemoapp/
│   ├── MainActivity.kt                      # Jetpack Compose Digital Instrument Cluster UI
│   ├── VhalDemoApplication.kt               # Hilt Application Class
│   ├── boot/BootCompletedReceiver.kt        # Boot Receiver
│   ├── data/
│   │   ├── CarInfoData.kt                   # Vehicle Info Data Class
│   │   └── VehiclePropertyRepository.kt     # VHAL CarPropertyManager Repository
│   ├── model/
│   │   └── VehicleUiState.kt                # Immutable UI State Model
│   ├── service/
│   │   └── CarPropertyMonitoringService.kt  # Background Car Service Monitor
│   └── ui/
│       └── VhalViewModel.kt                 # Hilt ViewModel for StateFlow UI Binding
├── aosp_integration/                        # AOSP System Image Baking Blueprint
│   ├── Android.bp                           # AOSP Module Build File
│   ├── com.example.vhaldemoapp.xml          # Privileged Permission Whitelist
│   └── README_AOSP_BAKING.md                # Production AOSP Build Guide
├── push_priv_app.ps1                        # Automated System App Deployment Script
└── README.md                                # Comprehensive Project Documentation
```
