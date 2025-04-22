package com.shubham.securenotesandroid.presentation.models

sealed class AuthState<out L, out R> {
    data object Idle : AuthState<Nothing, Nothing>()
    data object Loading : AuthState<Nothing, Nothing>()
    data class Success<L>(val data: L) : AuthState<L, Nothing>()
    data class Error<R>(val message: String) : AuthState<Nothing, R>()
}