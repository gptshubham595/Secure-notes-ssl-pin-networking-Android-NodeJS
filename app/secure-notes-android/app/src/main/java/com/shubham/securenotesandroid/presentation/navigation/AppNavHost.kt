package com.shubham.securenotesandroid.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shubham.securenotesandroid.presentation.models.UserState
import com.shubham.securenotesandroid.presentation.viewmodels.AuthViewModel
import com.shubham.securenotesandroid.presentation.views.CreateNoteScreen
import com.shubham.securenotesandroid.presentation.views.EditNoteScreen
import com.shubham.securenotesandroid.presentation.views.LoginScreen
import com.shubham.securenotesandroid.presentation.views.NoteDetailScreen
import com.shubham.securenotesandroid.presentation.views.NotesListScreen
import com.shubham.securenotesandroid.presentation.views.RegisterScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel()
    val userState = authViewModel.userState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = when (userState.value) {
            is UserState.LoggedIn -> Screen.NotesList.route
            else -> Screen.Login.route
        },
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.NotesList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.NotesList.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.NotesList.route) {
            NotesListScreen(
                onNoteClick = { noteId ->
                    navController.navigate(
                        Screen.NoteDetail.createRoute(
                            noteId
                        )
                    )
                },
                onCreateNoteClick = { navController.navigate(Screen.CreateNote.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
            NoteDetailScreen(
                noteId = noteId,
                onEditClick = { navController.navigate(Screen.EditNote.createRoute(noteId)) },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.CreateNote.route) {
            CreateNoteScreen(
                onNoteCreated = {
                    navController.navigate(Screen.NotesList.route) {
                        popUpTo(Screen.NotesList.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.EditNote.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
            EditNoteScreen(
                noteId = noteId,
                onNoteUpdated = {
                    navController.navigate(Screen.NoteDetail.createRoute(noteId)) {
                        popUpTo(Screen.NoteDetail.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}