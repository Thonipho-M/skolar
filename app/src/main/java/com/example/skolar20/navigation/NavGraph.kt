package com.example.skolar20.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import com.example.skolar20.ui.screens.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SkolarNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavDestination.Home.route) {
        composable(NavDestination.Home.route) { HomeScreen(navController) }
        composable(NavDestination.Tutors.route) { TutorsScreen(navController) }
        composable(NavDestination.Bookings.route) { BookingsScreen(navController) }
        composable(NavDestination.Messages.route) { ComingSoonScreen() }
        composable(NavDestination.Settings.route) { SettingsScreen() }

        // booking_new?tutorId=<id> (tutorId is optional)
        composable(
            route = "$ROUTE_NEW_BOOKING?tutorId={tutorId}",
            arguments = listOf(navArgument("tutorId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStack ->
            val tutorId = backStack.arguments?.getString("tutorId")
            NewBookingScreen(navController, preselectedTutorId = tutorId)
        }
    }
}
