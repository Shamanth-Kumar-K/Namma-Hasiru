package com.example.hasiru.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.hasiru.repository.AuthRepository

class AuthViewModel : ViewModel() {
    // 1. Link to the Repository we just made
    private val repository = AuthRepository()

    // 2. These states tell the UI what to show
    var isLoading = mutableStateOf(false)
    var loginSuccess = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    fun login(email: String, pass: String) {
        isLoading.value = true
        errorMessage.value = null

        repository.loginUser(email, pass) { success ->
            isLoading.value = false
            if (success) {
                loginSuccess.value = true
            } else {
                errorMessage.value = "Login Failed. Please check your credentials."
            }
        }
    }

    // In AuthViewModel.kt
    fun signUp(email: String, pass: String, onResult: (Boolean) -> Unit = {}) {
        isLoading.value = true
        errorMessage.value = null

        repository.signUpUser(email, pass) { success, error ->
            isLoading.value = false
            if (success) {
                // loginSuccess.value = true // Commented out to allow profile update before navigation
            } else {
                errorMessage.value = error ?: "Sign Up Failed"
            }
            onResult(success)
        }
    }

    fun onGoogleSignInResult(idToken: String) {
        isLoading.value = true
        repository.signInWithGoogle(idToken) { success, error ->
            isLoading.value = false
            if (success) {
                loginSuccess.value = true
            } else {
                errorMessage.value = error
            }
        }
    }

    // Add this to your AuthViewModel
    val currentUser
        get() = repository.getCurrentUser()

    val isUserLoggedIn: Boolean
        get() = repository.getCurrentUser() != null

    fun logout() {
        repository.logout()
        loginSuccess.value = false
    }

    fun updateProfile(name: String, photoUrl: String? = null, onResult: (Boolean) -> Unit) {
        isLoading.value = true
        errorMessage.value = null
        repository.updateProfile(name, photoUrl) { success ->
            isLoading.value = false
            if (!success) {
                errorMessage.value = "Failed to update profile."
            }
            onResult(success)
        }
    }
}
