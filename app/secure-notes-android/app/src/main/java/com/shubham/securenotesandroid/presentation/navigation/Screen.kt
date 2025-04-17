package com.shubham.securenotesandroid.presentation.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login_screen")
    data object Register : Screen("register_screen")
    data object NotesList : Screen("notes_list_screen")
    data object NoteDetail : Screen("note_detail_screen/{noteId}") {
        fun createRoute(noteId: String) = "note_detail_screen/$noteId"
    }
    data object CreateNote : Screen("create_note_screen")
    data object EditNote : Screen("edit_note_screen/{noteId}") {
        fun createRoute(noteId: String) = "edit_note_screen/$noteId"
    }
}