package com.shubham.securenotesandroid.core.data.network

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

            // Certificate pinning
            certificatePinner(
                okhttp3.CertificatePinner.Builder()
                    .add(
                        "192.168.69.188", // Replace with your actual hostname when moving to production
                        "sha256/ola+zKAlqr0M8j7FEUTUEn1cO6Gqx0uk3A==" // Replace with your actual certificate hash
                    )
                    .build()
            )
        }.build()
    }
}