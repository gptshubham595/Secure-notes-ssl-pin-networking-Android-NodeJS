package com.shubham.securenotesandroid.core.data.network

import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CertificatePinner @Inject constructor() {

    fun createOkHttpClient(interceptors: List<Interceptor>): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // Add all provided interceptors
            interceptors.forEach { addInterceptor(it) }

            // Set timeouts
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)

            val sslShaKeySecretRoot = "sha256/WmFszXEkOr5J5Bj1JmvmKNU/lwpuxMNxjy0qPQilvYk="
            val pins = listOf(
//                sslShaKeySecretOldRoot.toString(),
                sslShaKeySecretRoot,
            ).toTypedArray()

            val sslPattern = "*.ngrok-free.app"

            if (sslPattern != null) {
                // Certificate pinning
                val certificatePinner = CertificatePinner.Builder()
                    .add(sslPattern, *pins)
                    .build()
                certificatePinner(certificatePinner)
            }
        }.build()
    }
}