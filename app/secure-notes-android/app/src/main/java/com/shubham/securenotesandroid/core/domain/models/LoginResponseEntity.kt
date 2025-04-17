package com.shubham.securenotesandroid.core.domain.models

data class LoginResponseEntity(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)