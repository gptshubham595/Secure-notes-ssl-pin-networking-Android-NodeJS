package com.shubham.securenotesandroid.core.data.network

import com.shubham.securenotesandroid.core.data.models.LoginRequest
import com.shubham.securenotesandroid.core.data.models.LoginResponse
import com.shubham.securenotesandroid.core.data.models.LogoutRequest
import com.shubham.securenotesandroid.core.data.models.LogoutResponse
import com.shubham.securenotesandroid.core.data.models.RefreshTokenRequest
import com.shubham.securenotesandroid.core.data.models.RefreshTokenResponse
import com.shubham.securenotesandroid.core.data.models.RegisterRequest
import com.shubham.securenotesandroid.core.data.models.RegisterResponse
import com.shubham.securenotesandroid.core.data.models.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): RegisterResponse

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): RefreshTokenResponse

    @POST("api/auth/logout")
    suspend fun logout(@Body logoutRequest: LogoutRequest): LogoutResponse

    @GET("api/auth/user")
    suspend fun getCurrentUser(): UserResponse
}
