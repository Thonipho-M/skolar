package com.example.skolar20.navigation



import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.skolar20.ui.screens.*

@Composable
fun SkolarNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = NavDestination.Home.route) {
        composable(NavDestination.Home.route) { HomeScreen(navController) }
        composable(NavDestination.Tutors.route) { TutorsScreen() }     // ✅ working
        composable(NavDestination.Bookings.route) { ComingSoonScreen() } // 🚧
        composable(NavDestination.Messages.route) { ComingSoonScreen() } // 🚧
        composable(NavDestination.Settings.route) { SettingsScreen() } // ✅ simple
    }
}
