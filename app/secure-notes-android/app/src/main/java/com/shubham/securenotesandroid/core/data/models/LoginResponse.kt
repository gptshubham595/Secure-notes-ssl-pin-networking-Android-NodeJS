package com.shubham.securenotesandroid.core.data.models

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)