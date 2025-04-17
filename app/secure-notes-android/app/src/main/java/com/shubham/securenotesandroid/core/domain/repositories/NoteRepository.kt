package com.shubham.securenotesandroid.core.domain.repositories

import com.shubham.securenotesandroid.core.data.models.Note

interface NoteRepository {
    suspend fun getAllNotes(): Result<List<Note>>
    suspend fun getNote(noteId: String): Result<Note>
    suspend fun createNote(title: String, content: String): Result<Note>
    suspend fun updateNote(noteId: String, title: String, content: String): Result<Note>
    suspend fun deleteNote(noteId: String): Result<Boolean>
}