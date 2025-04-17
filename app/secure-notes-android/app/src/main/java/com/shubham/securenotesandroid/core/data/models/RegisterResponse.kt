package com.shubham.securenotesandroid.core.data.models

data class RegisterResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)