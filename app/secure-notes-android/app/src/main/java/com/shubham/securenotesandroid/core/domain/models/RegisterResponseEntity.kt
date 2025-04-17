package com.shubham.securenotesandroid.core.domain.models

data class RegisterResponseEntity(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)