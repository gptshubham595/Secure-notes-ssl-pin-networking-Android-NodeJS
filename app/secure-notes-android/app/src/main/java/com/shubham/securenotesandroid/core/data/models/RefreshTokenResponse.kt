package com.shubham.securenotesandroid.core.data.models

data class RefreshTokenResponse(
    val accessToken: String,
    val expiresIn: Int
)