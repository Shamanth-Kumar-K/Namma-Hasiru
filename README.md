# Hasiru - Tree Plantation & Monitoring App

**Hasiru** (meaning *Green* in Kannada) is an Android application developed as part of my internship project. The app is designed to empower environmental enthusiasts to register, track, and monitor tree plantations using modern mobile technologies.

---

## 🌟 Project Overview
The primary goal of Hasiru is to ensure the long-term sustainability of tree plantation drives. By leveraging real-time GPS tagging and cloud-based monitoring, the app allows users to document the growth of every sapling they plant, creating a transparent and verifiable record of environmental impact.

---

## 🚀 Key Features

### 1. Smart Plantation Registration
- **GPS Tagging:** Captures precise coordinates using high-accuracy GPS for every new plantation.
- **Visual Documentation:** Integrated camera system allowing users to take live photos or select from the gallery during registration.
- **Species Logging:** Support for common and scientific names with detailed notes on soil conditions.

### 2. Real-time Monitoring & Growth Tracking
- **Interactive Map:** A custom Google Maps interface to visualize all plantation sites globally.
- **Status Updates:** Users can update the health status of trees (*Alive*, *Dead*, or *Unknown*) and upload new "Growth Photos."
- **Growth Timeline:** Maintains a chronological history of status updates for every registered tree.

### 3. User Dashboard & Engagement
- **Secure Authentication:** Multi-method login via Email/Password and Google One-Tap Sign-In.
- **Impact Stats:** Personal dashboard showing "Trees Planted" and "Areas Covered."
- **Maintenance Reminders:** Automated background notifications via `WorkManager` scheduled 90 days post-plantation to remind users of checkups.

---

## 🛠 Tech Stack & Architecture

- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Declarative UI)
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend (Firebase):**
  - **Firestore:** Real-time NoSQL database for plant metadata and status history.
  - **Cloud Storage:** Secure storage for high-resolution growth and profile photos.
  - **Authentication:** Secure identity management.
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/)
- **Background Tasks:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)

---

## 📂 Project Structure

```text
com.example.hasiru/
├── repository/       # Data layer (Firebase interactions)
├── viewmodel/        # Business logic & UI state management
├── screens/          # Jetpack Compose UI components
├── ui/theme/         # Material3 Design System implementation
├── utils/            # Utilities (Camera, File, Storage, Location)
└── worker/           # Background tasks for reminders
```

---

## 🛠 Setup & Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/hasiru.git
   ```
2. **Add Firebase:**
   - Place your `google-services.json` in the `app/` directory.
   - Enable Auth (Email/Google), Firestore, and Storage in the Firebase Console.
3. **Build & Run:**
   - Sync Gradle and run on a physical device for full camera and GPS functionality.

---

## 📄 Additional Documentation
For more details on the backend architecture and Firestore schemas, see the [Backend Components Guide](Backend_components.md).

---
*Developed from ❤️ for a Greener Planet🌍.*
