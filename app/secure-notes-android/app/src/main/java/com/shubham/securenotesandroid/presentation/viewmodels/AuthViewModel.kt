package com.shubham.securenotesandroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.securenotesandroid.core.domain.models.LoginResponseEntity
import com.shubham.securenotesandroid.core.domain.repositories.AuthRepository
import com.shubham.securenotesandroid.presentation.models.AuthState
import com.shubham.securenotesandroid.presentation.models.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState =
        MutableStateFlow<AuthState<LoginResponseEntity, String>>(AuthState.Idle)
    val authState: StateFlow<AuthState<LoginResponseEntity, String>> = _authState.asStateFlow()

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
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading

            val result = authRepository.login(email, password)

            result.onSuccess { it: LoginResponseEntity ->
                loadUserInfo()
                _authState.value = AuthState.Success(it)
            }.onFailure {
                _authState.value =
                    AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading

            val result = authRepository.register(email, password)
            // Handle the result of the registration
            result.onSuccess { it: LoginResponseEntity ->
                loadUserInfo()
                _authState.value = AuthState.Success(it)
            }.onFailure {
                // Handle the error
                _authState.value =
                    AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading

            val result = authRepository.logout()

            // Consider logout successful even if API call fails
            // The important part is clearing the local tokens
            _userState.value = UserState.NotLoggedIn
            _authState.value = AuthState.Idle
        }
    }

    private fun loadUserInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            _userState.value = UserState.Loading

            val result = authRepository.getCurrentUser()
            result.onSuccess {
                _userState.value = UserState.LoggedIn(it)
            }.onFailure {
                _userState.value =
                    UserState.Error(result.exceptionOrNull()?.message ?: "Failed to load user")
            }
        }
    }


}