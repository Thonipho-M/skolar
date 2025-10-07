package com.example.skolar20.data.model



data class Tutor(
    val tutorId: String,
    val name: String,
    val expertise: List<String>,
    val qualifications: String,
    val rate: Double,
    val location: String
)
