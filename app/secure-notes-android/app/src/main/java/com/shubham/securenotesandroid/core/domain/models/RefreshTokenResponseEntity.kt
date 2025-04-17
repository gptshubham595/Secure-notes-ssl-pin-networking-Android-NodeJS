package com.shubham.securenotesandroid.core.domain.models

data class RefreshTokenResponseEntity(
    val accessToken: String,
    val expiresIn: Int
)