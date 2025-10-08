package com.example.skolar20.data.model


import java.time.Instant

data class Booking(
    val bookingId: String,
    val userId: String,
    val tutorId: String,
    val tutorName: String?,   // stored at create time for easy display
    val subject: String,
    val bookingTime: Instant, // UTC
    val status: String,       // e.g., "requested"
    val notes: String?
)

data class BookingCreate(
    val tutorId: String,
    val tutorName: String?,
    val userId: String,
    val subject: String,
    val bookingTime: Instant, // UTC
    val notes: String?
)
