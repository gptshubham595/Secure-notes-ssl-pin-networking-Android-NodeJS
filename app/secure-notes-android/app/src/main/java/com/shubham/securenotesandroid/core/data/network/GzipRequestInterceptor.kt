package com.shubham.securenotesandroid.core.data.network

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.GzipSink
import okio.buffer
import java.io.IOException

class GzipRequestInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip compression for small requests
        if (shouldSkipCompression(originalRequest)) {
            return chain.proceed(originalRequest)
        }
        
        val compressedRequest = originalRequest.newBuilder()
            .header("Content-Encoding", "gzip")
            .header("Accept-Encoding", "gzip")
            .method(originalRequest.method, gzip(originalRequest.body))
            .build()
            
        return chain.proceed(compressedRequest)
    }
    
    private fun shouldSkipCompression(request: Request): Boolean {
        // Skip GET requests (usually don't have a body to compress)
        if (request.method == "GET") return true
        
        // Skip if already compressed
        if (request.header("Content-Encoding") != null) return true
        
        // Skip if no body
        return request.body == null
    }
    
    private fun gzip(body: RequestBody?): RequestBody? {
        if (body == null) return null
        
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return body.contentType()
            }
            
            override fun contentLength(): Long {
                return -1 // We don't know the compressed length in advance
            }
            
            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                val gzipSink = GzipSink(sink).buffer()
                body.writeTo(gzipSink)
                gzipSink.close()
            }
        }
    }
}