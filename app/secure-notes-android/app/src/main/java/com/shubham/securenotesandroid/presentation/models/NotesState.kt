package com.shubham.securenotesandroid.presentation.models

import com.shubham.securenotesandroid.core.data.models.Note

sealed class NotesState {
    data object Loading : NotesState()
    data class Success(val notes: List<Note>) : NotesState()
    data class Error(val message: String) : NotesState()
}

    