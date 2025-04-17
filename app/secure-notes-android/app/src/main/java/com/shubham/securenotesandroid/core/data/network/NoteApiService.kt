package com.shubham.securenotesandroid.core.data.network

import com.shubham.securenotesandroid.core.data.models.DeleteNoteResponse
import com.shubham.securenotesandroid.core.data.models.Note
import com.shubham.securenotesandroid.core.data.models.NoteRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface NoteApiService {
    @GET("api/notes")
    suspend fun getNotes(): List<Note>
    
    @GET("api/notes/{id}")
    suspend fun getNote(@Path("id") noteId: String): Note
    
    @POST("api/notes")
    suspend fun createNote(@Body note: NoteRequest): Note
    
    @PUT("api/notes/{id}")
    suspend fun updateNote(@Path("id") noteId: String, @Body note: NoteRequest): Note
    
    @DELETE("api/notes/{id}")
    suspend fun deleteNote(@Path("id") noteId: String): DeleteNoteResponse
}