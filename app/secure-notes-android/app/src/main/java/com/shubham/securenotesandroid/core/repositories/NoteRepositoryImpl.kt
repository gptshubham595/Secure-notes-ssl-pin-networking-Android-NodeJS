package com.shubham.securenotesandroid.core.repositories

import com.shubham.securenotesandroid.core.data.models.Note
import com.shubham.securenotesandroid.core.data.models.NoteRequest
import com.shubham.securenotesandroid.core.data.network.NoteApiService
import com.shubham.securenotesandroid.core.domain.repositories.NoteRepository
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteApiService: NoteApiService
) : NoteRepository {

    override suspend fun getAllNotes(): Result<List<Note>> {
        return try {
            val notes = noteApiService.getNotes()
            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNote(noteId: String): Result<Note> {
        return try {
            val note = noteApiService.getNote(noteId)
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createNote(title: String, content: String): Result<Note> {
        return try {
            val note = noteApiService.createNote(NoteRequest(title, content))
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNote(noteId: String, title: String, content: String): Result<Note> {
        return try {
            val note = noteApiService.updateNote(noteId, NoteRequest(title, content))
            Result.success(note)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Boolean> {
        return try {
            noteApiService.deleteNote(noteId)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}