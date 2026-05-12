package com.example.hasiru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.hasiru.screens.*
import com.example.hasiru.ui.theme.HasiruTheme
import com.example.hasiru.viewmodel.AuthViewModel
import com.example.hasiru.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HasiruTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()

    val startDest = if (authViewModel.isUserLoggedIn) "main" else "welcome"

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(startDest) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("welcome") {
            WelcomeScreen(
                authViewModel = authViewModel,
                onGoogleSignIn = {
                    navController.navigate("main") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onContinueWithEmail = { navController.navigate("login") },
                onCreateAccount = { navController.navigate("signup") }
            )
        }

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onSignUp = { navController.navigate("signup") },
                onForgotPassword = { navController.navigate("forgot") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("signup") {
            SignUpScreen(
                authViewModel = authViewModel,
                onSignUpSuccess = {
                    navController.navigate("main") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onAlreadyHaveAccount = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("forgot") {
            ForgotPasswordScreen(
                onSendResetLink = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // --- UPDATED MAIN ROUTE ---
        composable("main") {
            MainScreen(
                navController = navController,
                // Add this to handle the (+) button click from the HomeScreen inside MainScreen
                onAddPlantClick = {
                    navController.navigate("add_plant")
                },
                onSignOut = {
                    authViewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- ADD PLANT ROUTE ---
        composable("add_plant") {
            AddPlantScreen(
                onPlantSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable("plantationList") {
            PlantationListScreen(
                plants = homeViewModel.plants,
                onBack = { navController.popBackStack() },
                onPlantClick = { plant -> navController.navigate("plantDetail/${plant.id}") },
                onDelete = { plant -> homeViewModel.deletePlant(plant) }
            )
        }

        composable(
            "plantDetail/{plantId}",
            deepLinks = listOf(navDeepLink { uriPattern = "hasiru://plant/{plantId}" })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId")?.toIntOrNull()
            val plant = homeViewModel.plants.find { it.id == plantId }
            if (plant != null) {
                PlantDetailScreen(
                    plant = plant,
                    onBack = { navController.popBackStack() },
                    onUpdateStatus = { id -> navController.navigate("statusUpdate/$id") }
                )
            } else {
                navController.popBackStack()
            }
        }

        // Note: NewPlantScreen and AddPlantScreen seem similar.
        // We are using "add_plant" for your GPS tagging version.
        composable("newPlant") {
            NewPlantScreen(
                onBack = { navController.popBackStack() },
                onPlantLogged = {
                    navController.popBackStack()
                }
            )
        }

        composable("statusUpdate/{plantId}") { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId")?.toIntOrNull() ?: 1
            val plant = homeViewModel.plants.find { it.id == plantId }
            StatusUpdateScreen(
                plantId = plantId,
                documentId = plant?.documentId ?: "",
                plantName = plant?.name ?: "Unknown Tree",
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable("speciesGuide") {
            SpeciesGuideScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("profileSettings") {
            ProfileSettingsScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
    }
}