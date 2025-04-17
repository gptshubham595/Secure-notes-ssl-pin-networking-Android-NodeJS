package com.shubham.securenotesandroid.presentation.models

import com.shubham.securenotesandroid.core.data.models.Note

sealed class NoteDetailState {
    data object Idle : NoteDetailState()
    data object Loading : NoteDetailState()
    data class Success(val note: Note) : NoteDetailState()
    data class Error(val message: String) : NoteDetailState()
}

    