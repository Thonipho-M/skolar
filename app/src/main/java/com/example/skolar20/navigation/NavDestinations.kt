package com.example.skolar20.navigation

enum class NavDestination(val route: String, val label: String) {
    Home("home", "Home"),
    Tutors("tutors", "Tutors"),
    Bookings("bookings", "Bookings"),
    Messages("messages", "Messages"),
    Settings("settings", "Settings")
}

// Extra route (not in bottom bar)
const val ROUTE_NEW_BOOKING = "booking_new"
