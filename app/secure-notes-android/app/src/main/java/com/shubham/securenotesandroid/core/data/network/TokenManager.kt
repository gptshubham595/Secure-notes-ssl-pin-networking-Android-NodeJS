package com.shubham.securenotesandroid.core.data.network

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptedSharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }

    fun saveAccessToken(token: String) {
        encryptedSharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
        
        // Calculate expiry time (current time + 5 minutes)
        val expiryTime = System.currentTimeMillis() + 5 * 60 * 1000
        encryptedSharedPreferences.edit()
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .apply()
    }

    fun saveRefreshToken(token: String) {
        encryptedSharedPreferences.edit()
            .putString(KEY_REFRESH_TOKEN, token)
            .apply()
    }

    fun getAccessToken(): String? {
        return encryptedSharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return encryptedSharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun isTokenExpired(): Boolean {
        val expiryTime = encryptedSharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() > expiryTime
    }

    fun clearTokens() {
        encryptedSharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .apply()
    }
}