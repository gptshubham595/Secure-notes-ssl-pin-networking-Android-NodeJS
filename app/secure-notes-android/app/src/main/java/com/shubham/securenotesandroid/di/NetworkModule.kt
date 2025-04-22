package com.shubham.securenotesandroid.di


import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.shubham.securenotesandroid.BuildConfig
import com.shubham.securenotesandroid.core.data.network.AuthApiService
import com.shubham.securenotesandroid.core.data.network.AuthInterceptor
import com.shubham.securenotesandroid.core.data.network.CertificatePinner
import com.shubham.securenotesandroid.core.data.network.NoteApiService
import com.shubham.securenotesandroid.core.data.network.TokenAuthenticator
import com.shubham.securenotesandroid.core.data.network.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    const val BASE_URL = "https://c691-123-63-206-73.ngrok-free.app"

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context,
        encryptedSharedPreferences: SharedPreferences
    ): TokenManager {
        return TokenManager(context, encryptedSharedPreferences)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }


    // Create a basic OkHttpClient without the authenticator
    @Provides
    @Singleton
    @Named("baseOkHttpClient")
    fun provideBaseOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        certificatePinner: CertificatePinner
    ): OkHttpClient {
        val interceptors = listOf(
            authInterceptor,
            loggingInterceptor,
        )

        return certificatePinner.createOkHttpClient(interceptors)
    }

    // Create a Retrofit instance for Auth API using the base OkHttpClient
    @Provides
    @Singleton
    @Named("authRetrofit")
    fun provideAuthRetrofit(
        @Named("baseOkHttpClient") okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Provide AuthApiService using the dedicated Retrofit instance
    @Provides
    @Singleton
    fun provideAuthApiService(
        @Named("authRetrofit") retrofit: Retrofit
    ): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    // Create the TokenAuthenticator with the AuthApiService
    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenManager: TokenManager,
        authApiService: AuthApiService
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenManager, authApiService)
    }

    // Now create the final OkHttpClient with the authenticator
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @Named("baseOkHttpClient") baseOkHttpClient: OkHttpClient,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return baseOkHttpClient.newBuilder()
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideNoteApiService(retrofit: Retrofit): NoteApiService {
        return retrofit.create(NoteApiService::class.java)
    }
}