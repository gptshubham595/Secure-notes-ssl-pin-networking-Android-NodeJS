package com.shubham.securenotesandroid.core.domain.repositories

import com.shubham.securenotesandroid.core.domain.models.LoginResponseEntity
import com.shubham.securenotesandroid.core.domain.models.LogoutResponseEntity
import com.shubham.securenotesandroid.core.domain.models.RefreshTokenResponseEntity
import com.shubham.securenotesandroid.core.domain.models.RegisterResponseEntity
import com.shubham.securenotesandroid.core.domain.models.UserResponseEntity

interface AuthRepository {
    suspend fun register(email: String, password: String): Result<RegisterResponseEntity>
    suspend fun login(email: String, password: String): Result<LoginResponseEntity>
    suspend fun refreshToken(): Result<RefreshTokenResponseEntity>
    suspend fun logout(): Result<LogoutResponseEntity>
    suspend fun getCurrentUser(): Result<UserResponseEntity>
    fun isLoggedIn(): Boolean
}