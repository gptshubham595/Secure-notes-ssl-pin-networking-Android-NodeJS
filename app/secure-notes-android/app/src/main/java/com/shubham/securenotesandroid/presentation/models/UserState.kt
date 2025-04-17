package com.shubham.securenotesandroid.presentation.models

import com.shubham.securenotesandroid.core.domain.models.UserResponseEntity

sealed class UserState {
    data object Loading : UserState()
    data object NotLoggedIn : UserState()
    data class LoggedIn(val user: UserResponseEntity) : UserState()
    data class Error(val message: String) : UserState()
}