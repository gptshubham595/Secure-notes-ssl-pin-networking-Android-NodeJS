package com.shubham.securenotesandroid.core.mappers

import com.shubham.securenotesandroid.core.data.models.LoginResponse
import com.shubham.securenotesandroid.core.data.models.LogoutResponse
import com.shubham.securenotesandroid.core.data.models.RefreshTokenResponse
import com.shubham.securenotesandroid.core.data.models.RegisterResponse
import com.shubham.securenotesandroid.core.data.models.UserResponse
import com.shubham.securenotesandroid.core.domain.models.LoginResponseEntity
import com.shubham.securenotesandroid.core.domain.models.LogoutResponseEntity
import com.shubham.securenotesandroid.core.domain.models.RefreshTokenResponseEntity
import com.shubham.securenotesandroid.core.domain.models.UserResponseEntity

fun LoginResponse.toDomain() = LoginResponseEntity(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
)

fun RegisterResponse.toDomain() = LoginResponseEntity(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
)


fun UserResponse.toDomain() = UserResponseEntity(
    _id = _id,
    email = email,
    createdAt = createdAt,
)

fun LogoutResponse.toDomain() = LogoutResponseEntity(
    msg = msg,
)

fun RefreshTokenResponse.toDomain() = RefreshTokenResponseEntity(
    accessToken = accessToken,
    expiresIn = expiresIn,
)