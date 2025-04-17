package com.shubham.securenotesandroid.presentation.models

sealed class NoteActionState {
        object Idle : NoteActionState()
        object Loading : NoteActionState()
        object Success : NoteActionState()
        data class Error(val message: String) : NoteActionState()
    }