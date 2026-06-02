# ToddlerLock 🔒

A robust, elegant, and secure utility app built natively for Android using **Kotlin**, **Jetpack Compose**, and the latest **Material 3 (M3) Natural Tones Design Theme**.

**ToddlerLock** is designed to prevent toddlers, kids, or accidental touch inputs from disrupting your active applications (like WhatsApp video calls, YouTube playback, or key active tasks) by dynamically capturing and swallowing all screen touches while preserving important operations.

---

## ✨ Features

- **Transparent Input Blocker**: Creates a zero-latency, full-screen transparent overlay using Android's native `WindowManager` that intercepts and consumes all touches, clicks, and drags.
- **Armed Hardware Shortcut**: Enable the shortcut in ToddlerLock, then hold **Volume Up** and **Volume Down** simultaneously for **3 physical seconds** to engage or disengage the touch blocker from another app.
- **Custom Adaptive Banner**: Display a clean, highly polished status notification on lock state activation reminding the parent how to unlock the device safely.
- **Natural Tones UI Theme**: Beautiful and distinct Material 3 color system featuring sage greens, warm biscuit tones, charcoal headers, and soft red warnings, designed to look clean and integrated.
- **Simple Configuration Screen**: The app screen is used to grant permissions and arm or disarm the global hardware shortcut.
- **Instructional Troubleshooter**: Comprehensive support for modern Android platforms including sideload warnings and Restricted Settings instructions.

---

## 🛠️ Architecture

The codebase follows modern Android development practices:

1. **`TouchBlockService` (Accessibility Service)**:
   - Operates globally to capture volume key interactions via `onKeyEvent`.
   - Employs a precise, non-blocking coroutine countdown key timer (exactly 3 seconds) for the dual-key trigger.
   - Dynamically manages a `TYPE_APPLICATION_OVERLAY` full-screen frame layout.
2. **`MainActivity` (Jetpack Compose UI)**:
   - Uses lifecycle-aware state collection (`collectAsStateWithLifecycle`) bound to the service state.
   - Checks permission states reactively, guides users through system preference intents, and arms or disarms the hardware shortcut.
3. **`MyApplicationTheme` (Natural Tones Styling)**:
   - Centered around an earth-toned color palette (`SageGreen`, `BiscuitBeige`, `Charcoal`, and `WarmBackground`).

---

## 🚀 Setup & Installation Instructions

To start blocking touch inputs, the system requires two runtime authorization steps:

### 1. Enable Display Over Other Apps (Overlay Permission)
This allows the lock engine to draw a full-screen block screen on top of other video players or messaging windows.
- In the app dashboard, click **Grant: Display Over Apps**.
- Select **ToddlerLock** from the system list and turn on **Allow display over other apps**.

### 2. Enable Accessibility Service
This allows ToddlerLock to monitor the physical Volume Up and Down keys, even when you block your screen inside other apps.
- On the dashboard, click **Enable Accessibility Service**.
- Select **ToddlerLock Controller** from the downloaded list and toggle it **On**.

---

## ⚠️ Android 13+ Restricted Settings Warning
Because ToddlerLock is installed as a custom utility app, newer Android platforms may block accessibility triggers by default (a greyed-out setting labeled *"Restricted Setting"*). 

**How to easily bypass this restiction:**
1. Navigate back to your Android launcher home screen.
2. **Long press** on the **ToddlerLock** app icon.
3. Tap **App Info** (the circular `(i)` settings button).
4. Tap the **Three Dots Menu** (`⋮`) in the top-right corner of the App Info screen.
5. select **Allow restricted settings** and confirm your device PIN or Fingerprint.
6. Re-open **ToddlerLock** and click **Enable Accessibility Service** to complete configuration!

---

## 🧪 Testing the Lock
Once both permissions are active:
1. Enable the ToddlerLock shortcut in the app.
2. Open WhatsApp, YouTube, or another target app.
3. Hold both volume buttons simultaneously for 3 seconds to engage the touch blocker.
4. Hold both volume buttons again for 3 seconds to disengage it.
5. Disable the shortcut in ToddlerLock when you want volume button presses to be ignored by the lock feature.
