package com.shubham.securenotesandroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.securenotesandroid.core.domain.repositories.NoteRepository
import com.shubham.securenotesandroid.presentation.models.NoteActionState
import com.shubham.securenotesandroid.presentation.models.NoteDetailState
import com.shubham.securenotesandroid.presentation.models.NotesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _notesState = MutableStateFlow<NotesState>(NotesState.Loading)
    val notesState: StateFlow<NotesState> = _notesState.asStateFlow()

    private val _noteDetailState = MutableStateFlow<NoteDetailState>(NoteDetailState.Idle)
    val noteDetailState: StateFlow<NoteDetailState> = _noteDetailState.asStateFlow()

    private val _noteActionState = MutableStateFlow<NoteActionState>(NoteActionState.Idle)
    val noteActionState: StateFlow<NoteActionState> = _noteActionState.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _notesState.value = NotesState.Loading

            val result = noteRepository.getAllNotes()

            _notesState.value = if (result.isSuccess) {
                NotesState.Success(result.getOrNull() ?: emptyList())
            } else {
                NotesState.Error(result.exceptionOrNull()?.message ?: "Failed to load notes")
            }
        }
    }

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _noteDetailState.value = NoteDetailState.Loading

            val result = noteRepository.getNote(noteId)

            _noteDetailState.value = if (result.isSuccess) {
                NoteDetailState.Success(result.getOrNull()!!)
            } else {
                NoteDetailState.Error(result.exceptionOrNull()?.message ?: "Failed to load note")
            }
        }
    }

    fun createNote(title: String, content: String) {
        viewModelScope.launch {
            _noteActionState.value = NoteActionState.Loading

            val result = noteRepository.createNote(title, content)

            _noteActionState.value = if (result.isSuccess) {
                loadNotes() // Refresh the list
                NoteActionState.Success
            } else {
                NoteActionState.Error(result.exceptionOrNull()?.message ?: "Failed to create note")
            }
        }
    }

    fun updateNote(noteId: String, title: String, content: String) {
        viewModelScope.launch {
            _noteActionState.value = NoteActionState.Loading

            val result = noteRepository.updateNote(noteId, title, content)

            _noteActionState.value = if (result.isSuccess) {
                loadNotes() // Refresh the list
                loadNote(noteId) // Refresh the detail view
                NoteActionState.Success
            } else {
                NoteActionState.Error(result.exceptionOrNull()?.message ?: "Failed to update note")
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _noteActionState.value = NoteActionState.Loading

            val result = noteRepository.deleteNote(noteId)

            _noteActionState.value = if (result.isSuccess) {
                loadNotes() // Refresh the list
                NoteActionState.Success
            } else {
                NoteActionState.Error(result.exceptionOrNull()?.message ?: "Failed to delete note")
            }
        }
    }

    fun resetNoteActionState() {
        _noteActionState.value = NoteActionState.Idle
    }


}