<div align="center">

# 🛡️ Suraksha-Setu — Hyper-Local Safety Network

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white" />
  <img src="https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white" />
  <img src="https://img.shields.io/badge/JavaMail_API-EA2D2E?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/OpenStreetMap-7EBC6F?style=for-the-badge&logo=openstreetmap&logoColor=white" />
</p>

A hyper-local emergency safety application designed to protect women and the elderly by immediately alerting a local "Circle of Trust" (neighbors, shopkeepers, volunteers) and local authorities within a 5-kilometer radius during critical situations.

</div>

---

## 🏗️ Architecture

### High-Level Architecture

```mermaid
graph TD
A[Android Frontend <br/> Kotlin + XML] --> B[Background Services <br/> Foreground Sensor Tracking]
B --> C[Shake & Voice Detectors <br/> SensorManager & SpeechRecognizer]
C --> D[Threat Detector <br/> Audio Amplitude Analysis]
B --> E[LocationTracker <br/> FusedLocationProviderClient]
C --> F[SOS Manager <br/> Alert Coordinator]
F --> G[SQLite Database <br/> Local Storage]
F --> H[SMS Fallback <br/> SmsManager]
F --> I[Email Dispatcher <br/> JavaMail API + SMTP]
```

### Emergency Alert Workflow

```mermaid
sequenceDiagram
participant User
participant Sensors
participant SafeCircle
participant Authorities

User->>Sensors: Shake Phone / Voice Command
Sensors->>Sensors: Detect Threat (Amplitude / Pattern)
Sensors->>User: Auto-record 30s Audio Evidence
Sensors->>SafeCircle: Dispatch Stage 1: Live GPS via SMS & Email
Sensors->>Authorities: Notify Volunteer Network
Sensors->>SafeCircle: Dispatch Stage 2: Audio Evidence attached via Email
```

---

## ✨ Core Features

### 🚨 Rapid SOS Dispatch
- **Hardware Trigger:** Shake the phone continuously to trigger an SOS, even if the app is closed or running in the background.
- **Voice Trigger:** Speak a predefined safe word or scream to activate the alarm.
- **AI Threat Detection:** Analyzes microphone amplitude to automatically detect panic/screaming.

### 👥 Safe Circle Management
- Add up to 5 trusted contacts (family, neighbors, local shopkeepers).
- Explicit, user-friendly UI for managing and deleting contacts.
- Immediate notification to all contacts via **SMS Fallback** and **Encrypted SMTP Emails**.

### 🕵️ Evidence Collection
- **Two-Stage SOS:** Instantly sends out live GPS coordinates to ensure rapid response.
- **Audio Capture:** Silently records 30 seconds of background audio during the emergency and emails it as an encrypted `.mp3` attachment to the Safe Circle.

### 🚓 Live Authority Tracking
- **Nearby Police:** Integrates completely free **OpenStreetMap Overpass API** to instantly locate all police stations within a 5km radius.
- **Interactive UI:** Tap a police station to instantly map directions via Google Maps.
- **One-Tap Dial:** Instantly dial National Emergency Number (112).

### ⚙️ Customizable Settings
- Toggle background Shake Detection to save battery.
- Toggle continuous Voice Command monitoring.
- Dark-mode optimized high-visibility UI with a massive SOS panic button.

---

## 🖼️ Application Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center" valign="top"><img src="Screenshots/1000760320.jpg" width="220" /><br/><b>Home Screen</b></td>
      <td align="center" valign="top"><img src="Screenshots/1000760322.png" width="220" /><br/><b>Safe Circle</b></td>
      <td align="center" valign="top"><img src="Screenshots/1000760321.jpg" width="220" /><br/><b>Alert History</b></td>
    </tr>
    <tr>
      <td align="center" valign="top"><img src="Screenshots/1000760323.jpg" width="220" /><br/><b>Live Police Maps</b></td>
      <td align="center" valign="top"><img src="Screenshots/1000760324.jpg" width="220" /><br/><b>Settings</b></td>
      <td align="center" valign="top"></td> <!-- Empty cell for alignment -->
    </tr>
  </table>
</div>

---

## 🚀 Running Locally — Step by Step

### Prerequisites
- [Android Studio Iguana+](https://developer.android.com/studio)
- [Java Development Kit (JDK) 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- A physical Android Device or Emulator running Android 9.0+

### Step 1 — Clone the Repository
```bash
git clone https://github.com/Abhinek8987/Suraksha-Setu.git
```

### Step 2 — Configure Email Credentials
This app uses direct SMTP to send emails. You must configure your Google App Password.
1. Open `app/src/main/java/com/example/surakshasetu/email/EmailSender.kt`
2. Update the credentials on line 20:
```kotlin
val username = "YOUR_GMAIL@gmail.com"
val password = "YOUR_16_DIGIT_APP_PASSWORD"
```

### Step 3 — Build and Run
1. Open the project folder in **Android Studio**.
2. Allow Gradle to sync.
3. Connect your Android device via USB (enable USB Debugging) or start an Emulator.
4. Click **Run** (`Shift + F10`).
5. **Critical:** Grant all requested permissions (Location, Microphone, SMS) for the background services to function correctly!

---

*Designed and Built for the safety of rural communities and the women workforce.*
