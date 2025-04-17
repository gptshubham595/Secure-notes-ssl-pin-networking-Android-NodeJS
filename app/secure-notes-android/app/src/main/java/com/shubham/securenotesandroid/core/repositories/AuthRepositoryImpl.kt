package com.shubham.securenotesandroid.core.repositories

import com.shubham.securenotesandroid.core.data.models.LoginRequest
import com.shubham.securenotesandroid.core.data.models.LogoutRequest
import com.shubham.securenotesandroid.core.data.models.RefreshTokenRequest
import com.shubham.securenotesandroid.core.data.models.RegisterRequest
import com.shubham.securenotesandroid.core.data.network.AuthApiService
import com.shubham.securenotesandroid.core.data.network.TokenManager
import com.shubham.securenotesandroid.core.domain.models.LoginResponseEntity
import com.shubham.securenotesandroid.core.domain.models.LogoutResponseEntity
import com.shubham.securenotesandroid.core.domain.models.RefreshTokenResponseEntity
import com.shubham.securenotesandroid.core.domain.models.RegisterResponseEntity
import com.shubham.securenotesandroid.core.domain.models.UserResponseEntity
import com.shubham.securenotesandroid.core.domain.repositories.AuthRepository
import com.shubham.securenotesandroid.core.mappers.toDomain
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun register(email: String, password: String): Result<RegisterResponseEntity> {
        return try {
            val response = authApiService.register(RegisterRequest(email, password))
            tokenManager.saveAccessToken(response.accessToken)
            tokenManager.saveRefreshToken(response.refreshToken)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<LoginResponseEntity> {
        return try {
            val response = authApiService.login(LoginRequest(email, password))
            tokenManager.saveAccessToken(response.accessToken)
            tokenManager.saveRefreshToken(response.refreshToken)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(): Result<RefreshTokenResponseEntity> {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return Result.failure(Exception("No refresh token found"))

        return try {
            val response = authApiService.refreshToken(RefreshTokenRequest(refreshToken))
            tokenManager.saveAccessToken(response.accessToken)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            tokenManager.clearTokens()
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<LogoutResponseEntity> {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return Result.failure(Exception("No refresh token found"))

        return try {
            val response = authApiService.logout(LogoutRequest(refreshToken))
            tokenManager.clearTokens()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            tokenManager.clearTokens() // Clear anyway on failure
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<UserResponseEntity> {
        return try {
            val response = authApiService.getCurrentUser()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Boolean {
        return tokenManager.getRefreshToken() != null
    }
}