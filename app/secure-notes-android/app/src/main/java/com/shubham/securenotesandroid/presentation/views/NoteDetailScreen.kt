package com.shubham.securenotesandroid.presentation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shubham.securenotesandroid.presentation.models.NoteActionState
import com.shubham.securenotesandroid.presentation.models.NoteDetailState
import com.shubham.securenotesandroid.presentation.viewmodels.NotesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val noteDetailState = viewModel.noteDetailState.collectAsState()
    val noteActionState = viewModel.noteActionState.collectAsState()

    // Load note details when screen is launched
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Only show edit button if note is loaded
                    if (noteDetailState.value is NoteDetailState.Success) {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Note"
                            )
                        }
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
                    val note = (noteDetailState.value as NoteDetailState.Success).note

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            text = formatDate(note.updatedAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyLarge
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
                            text = (noteDetailState.value as NoteDetailState.Error).message,
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

            // Show confirmation dialog when delete is successful
            if (noteActionState.value is NoteActionState.Success) {
                LaunchedEffect(Unit) {
                    // Reset action state after navigation
                    viewModel.resetNoteActionState()
                    onBackClick()
                }
            }
        }
    }
}