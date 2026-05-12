# Project Summary: Hasiru (Green Movement App)

## Overview
**Hasiru** (meaning "Green" in Kannada) is a modern Android application designed to empower citizens in Bangalore to transform the city back into a "Garden City." The app allows users to log new plantations, track tree growth, and visualize the green movement through an interactive map.

## Frontend Architecture
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Modern, Declarative UI)
- **Navigation**: Jetpack Compose Navigation (Type-safe routes)
- **Theme**: Material 3 (Material Design with custom green color palette)
- **State Management**: ViewModel + Kotlin Coroutines/Flow

## Key Features & Screens
1.  **Onboarding**:
    *   `SplashScreen`: Animated entry.
    *   `WelcomeScreen`: Entry point for authentication.
2.  **Authentication**:
    *   `LoginScreen`, `SignUpScreen`, `ForgotPasswordScreen`: Email/Password and Google Sign-In integration.
3.  **Core Experience**:
    *   `HomeScreen`: Overview of activity and recent plantations.
    *   `MapScreen`: Interactive visualization of tree locations.
    *   `PlantationListScreen`: Categorized view of all trees.
4.  **Logging & Tracking**:
    *   `NewPlantScreen`: Camera and location integration to log a new tree.
    *   `PlantDetailScreen`: Comprehensive view of a tree's history and health.
    *   `StatusUpdateScreen`: Record growth milestones and health status updates.
5.  **Engagement**:
    *   `ProfileScreen`: User stats and gamified badges.
    *   `SpeciesGuideScreen`: Educational resource for local flora.

## Project Structure
- `com.example.hasiru.screens`: UI Components and Composables.
- `com.example.hasiru.ui.auth`: Authentication logic and helpers.
- `com.example.hasiru.data.repository`: Data abstraction layer.
- `com.example.hasiru.ui.theme`: Global styling and typography.
