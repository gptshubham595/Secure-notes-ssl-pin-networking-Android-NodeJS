package com.shubham.securenotesandroid.core.data.network

import com.shubham.securenotesandroid.core.data.models.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.security.Provider
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authServiceProvider: AuthApiService  // Use Provider instead of direct instance
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Skip if the response is from the refresh token endpoint
        if (response.request.url.toString().contains("/api/auth/refresh")) {
            return null
        }

        // Get refresh token from storage
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        try {
            // Get the service only when needed
            val authService = authServiceProvider

            // Execute the refresh call synchronously
            val refreshResponse = runBlocking {
                authService.refreshToken(RefreshTokenRequest(refreshToken))
            }

            // Update the token in storage
            tokenManager.saveAccessToken(refreshResponse.accessToken)

            // Retry the original request with the new token
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshResponse.accessToken}")
                .build()

        } catch (e: Exception) {
            // If refresh fails, clear tokens and return null (will force logout)
            tokenManager.clearTokens()
            return null
        }
    }
}