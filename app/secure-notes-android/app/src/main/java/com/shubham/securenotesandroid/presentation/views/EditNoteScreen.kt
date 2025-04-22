package com.shubham.securenotesandroid.presentation.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shubham.securenotesandroid.presentation.models.NoteActionState
import com.shubham.securenotesandroid.presentation.models.NoteDetailState
import com.shubham.securenotesandroid.presentation.viewmodels.NotesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    noteId: String,
    onNoteUpdated: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val noteDetailState = viewModel.noteDetailState.collectAsState()
    val noteActionState = viewModel.noteActionState.collectAsState()

    val title = remember { mutableStateOf("") }
    val content = remember { mutableStateOf("") }
    val isDeleteDialogVisible = remember { mutableStateOf(false) }

    // Load note details when screen is launched
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    // Update local state when note is loaded
    LaunchedEffect(noteDetailState.value) {
        if (noteDetailState.value is NoteDetailState.Success) {
            val note = (noteDetailState.value as NoteDetailState.Success).note
            title.value = note.title
            content.value = note.content
        }
    }

    // Navigate when note is updated successfully
    LaunchedEffect(noteActionState.value) {
        if (noteActionState.value is NoteActionState.Success) {
            onNoteUpdated()
        }
    }

    BackHandler(onBack = onBackClick)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isDeleteDialogVisible.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    IconButton(
                        onClick = { viewModel.updateNote(noteId, title.value, content.value) },
                        enabled = title.value.isNotEmpty() && content.value.isNotEmpty() &&
                                noteActionState.value !is NoteActionState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Note"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (noteDetailState.value) {
                is NoteDetailState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is NoteDetailState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = title.value,
                            onValueChange = { title.value = it },
                            label = { Text("Title") },
                            maxLines = 1,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = content.value,
                            onValueChange = { content.value = it },
                            label = { Text("Content") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is NoteDetailState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.error
                        )

                        Text(
                            text = (noteDetailState as NoteDetailState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Button(
                            onClick = { viewModel.loadNote(noteId) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }

                else -> { /* Idle state - do nothing */
                }
            }

            // Show loading indicator
            if (noteActionState.value is NoteActionState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Show error message
            if (noteActionState.value is NoteActionState.Error) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text((noteActionState as NoteActionState.Error).message)
                }
            }
        }
    }

    // Delete confirmation dialog
    if (isDeleteDialogVisible.value) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogVisible.value = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleteDialogVisible.value = false
                        viewModel.deleteNote(noteId)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteDialogVisible.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}