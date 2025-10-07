package com.example.skolar20.ui


import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.skolar20.ui.screens.LoginScreen
import com.example.skolar20.navigation.SkolarNavGraph
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.skolar20.navigation.NavDestination
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Modifier

@Composable
fun App() {
    val auth = remember { FirebaseAuth.getInstance() }
    var user by remember { mutableStateOf(auth.currentUser) }

    // Listen for auth changes
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            user = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (user == null) {
        LoginScreen() // startup screen until signed in
    } else {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val current = backStack?.destination?.route ?: NavDestination.Home.route
        val items = listOf(
            NavDestination.Home,
            NavDestination.Tutors,
            NavDestination.Bookings,
            NavDestination.Messages,
            NavDestination.Settings
        )

        Scaffold(
            bottomBar = {
                NavigationBar {
                    items.forEach { dest ->
                        val selected = current == dest.route
                        val icon = when (dest) {
                            NavDestination.Home -> Icons.Default.Home
                            NavDestination.Tutors -> Icons.Default.AccountCircle
                            NavDestination.Bookings -> Icons.Default.Add
                            NavDestination.Messages -> Icons.Default.Email
                            NavDestination.Settings -> Icons.Default.Settings
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                }
                            },
                            icon = { Icon(icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        ) { inner -> Surface(Modifier.padding(inner)) { SkolarNavGraph(navController) } }
    }
}
