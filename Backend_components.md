# Backend Components - Hasiru App

This document outlines the Firebase services and backend components integrated into the **Hasiru** application to provide secure authentication, real-time data persistence, and cloud storage.

---

## 1. Firebase Authentication
**Role:** Managed user identity and secure access.

*   **Email/Password Authentication:** Allows users to create accounts and log in using traditional credentials.
*   **Google Sign-In:** Integrated using the Google Identity services and Firebase `signInWithCredential` for a seamless, one-tap onboarding experience.
*   **User Profiles:** Manages core user data including `displayName`, `email`, and `photoUrl` (linked to Firebase Storage).
*   **Session Management:** Persistent login states using `FirebaseAuth.currentUser` to keep users logged in across app restarts.

---

## 2. Cloud Firestore (NoSQL Database)
**Role:** Real-time data storage and synchronization.

*   **`users` Collection:** Stores extended user metadata such as:
    *   `treesPlanted`: Total count of trees registered by the user.
    *   `areasCovered`: Total unique locations tagged.
    *   `name`, `email`, `photoUrl`: Mirrored Auth data for easy querying.
*   **`plants` Collection:** The primary data store for registered trees:
    *   **Fields:** `name`, `scientificName`, `latitude`, `longitude`, `status`, `imageUrl`, `timestamp`.
    *   **Security:** Documents are associated with the `uid` of the creator to ensure private data access.
*   **`updates` Sub-collection:** Each plant document contains a sub-collection of status updates, tracking the growth history and health of the tree over time with timestamped photos.
*   **Real-time Listeners:** Uses `addSnapshotListener` to provide instant UI updates when plant data changes on the server.

---

## 3. Firebase Storage
**Role:** Persistent cloud storage for media assets.

*   **Profile Images (`profile_images/`):** Stores user profile pictures uploaded during registration or profile updates.
*   **Plant Photos (`plant_images/`):** Stores the initial high-resolution photo taken during the registration of a new tree.
*   **Growth Photos (`update_images/`):** Stores chronological photos taken during status updates to visually document tree growth.
*   **Public URLs:** Generates secure download URLs that are stored in Firestore for efficient image loading via the `Coil` library.

---

## 4. WorkManager Integration (Local Backend)
**Role:** Background task scheduling for maintenance reminders.

*   **`PlantReminderWorker`:** Although not a Firebase service, it works alongside the backend to schedule "Watering/Checkup" notifications.
*   **Logic:** Scheduled for 90 days after registration to remind users to check on their saplings, ensuring long-term sustainability.

---

## 5. Firebase Configuration
*   **`google-services.json`:** Contains the project-specific API keys and identifiers required for the Android app to communicate with Firebase services.
*   **Firebase BOM (Bill of Materials):** Used in `build.gradle.kts` to ensure all Firebase libraries (Auth, Firestore, Storage) are version-compatible.
