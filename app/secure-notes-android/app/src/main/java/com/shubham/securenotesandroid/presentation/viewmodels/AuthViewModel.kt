package com.shubham.securenotesandroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.securenotesandroid.core.domain.repositories.AuthRepository
import com.shubham.securenotesandroid.presentation.models.AuthState
import com.shubham.securenotesandroid.presentation.models.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    init {
        // Check if user is logged in
        if (authRepository.isLoggedIn()) {
            loadUserInfo()
        } else {
            _userState.value = UserState.NotLoggedIn
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authRepository.login(email, password)

            _authState.value = if (result.isSuccess) {
                loadUserInfo()
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authRepository.register(email, password)

            _authState.value = if (result.isSuccess) {
                loadUserInfo()
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = authRepository.logout()

            // Consider logout successful even if API call fails
            // The important part is clearing the local tokens
            _userState.value = UserState.NotLoggedIn
            _authState.value = AuthState.Idle
        }
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            _userState.value = UserState.Loading

            val result = authRepository.getCurrentUser()

            _userState.value = if (result.isSuccess) {
                UserState.LoggedIn(result.getOrNull()!!)
            } else {
                UserState.Error(result.exceptionOrNull()?.message ?: "Failed to load user")
            }
        }
    }


}