package com.example.skolar20.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.skolar20.data.remote.FirestoreService
import com.example.skolar20.data.model.BookingCreate
import com.example.skolar20.data.model.Tutor
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewBookingScreen(navController: NavController, preselectedTutorId: String?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val user = auth.currentUser

    var tutors by remember { mutableStateOf<List<Tutor>>(emptyList()) }
    var loadingTutors by remember { mutableStateOf(true) }
    var formBusy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    // Form fields
    var tutorId by remember { mutableStateOf<String?>(preselectedTutorId) }
    var subject by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Date & time (local) -> convert to UTC when saving
    val now = remember { ZonedDateTime.now() }
    var date by remember { mutableStateOf<LocalDate>(now.toLocalDate()) }
    var time by remember { mutableStateOf<LocalTime>(now.toLocalTime().withSecond(0).withNano(0)) }

    val dateLabel = remember(date) { date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
    val timeLabel = remember(time) { time.format(DateTimeFormatter.ofPattern("HH:mm")) }

    // Load tutors
    LaunchedEffect(Unit) {
        try {
            tutors = FirestoreService.fetchTutors()
            // If preselected id exists, keep it; else default to first tutor
            if (tutorId == null && tutors.isNotEmpty()) tutorId = tutors.first().tutorId
        } catch (e: Exception) {
            error = e.message
        } finally {
            loadingTutors = false
        }
    }

    fun tutorNameFor(id: String?): String? =
        tutors.firstOrNull { it.tutorId == id }?.name

    fun pickDate() {
        val c = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, y, m, d -> date = LocalDate.of(y, m + 1, d) },
            date.year, date.monthValue - 1, date.dayOfMonth
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    fun pickTime() {
        val c = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, h, min -> time = LocalTime.of(h, min) },
            time.hour, time.minute, true
        ).show()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("New Booking", style = MaterialTheme.typography.titleLarge)

        if (loadingTutors) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        // Tutor dropdown (simple)
        var showTutors by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = tutorNameFor(tutorId) ?: "Select tutor",
            onValueChange = {},
            readOnly = true,
            label = { Text("Tutor") },
            modifier = Modifier.fillMaxWidth().clickable { showTutors = true }
        )
        DropdownMenu(expanded = showTutors, onDismissRequest = { showTutors = false }) {
            tutors.forEach { t ->
                DropdownMenuItem(
                    text = { Text(t.name) },
                    onClick = { tutorId = t.tutorId; showTutors = false }
                )
            }
        }

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        // Date / Time pickers
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = dateLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier.weight(1f).clickable { pickDate() }
            )
            OutlinedTextField(
                value = timeLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Time") },
                modifier = Modifier.weight(1f).clickable { pickTime() }
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) Text("Error: $error", color = MaterialTheme.colorScheme.error)
        if (info != null) Text(info!!, color = MaterialTheme.colorScheme.primary)

        Button(
            onClick = {
                if (user == null) { error = "You must be signed in."; return@Button }
                if (tutorId == null) { error = "Please select a tutor."; return@Button }
                if (subject.isBlank()) { error = "Please enter a subject."; return@Button }

                scope.launch {
                    try {
                        formBusy = true; error = null; info = null
                        // Local -> UTC Instant
                        val localDT = LocalDateTime.of(date, time)
                        val instantUtc = localDT.atZone(ZoneId.systemDefault()).toInstant()

                        val token = user.getIdToken(false).await()?.token
                            ?: throw Exception("Could not get auth token")

                        val createdId = FirestoreService.createBooking(
                            BookingCreate(
                                tutorId = tutorId!!,
                                tutorName = tutorNameFor(tutorId),
                                userId = user.uid,
                                subject = subject.trim(),
                                bookingTime = instantUtc,
                                notes = notes.ifBlank { null }
                            ),
                            idToken = token
                        )
                        info = "Booking created (#$createdId)"
                        // Navigate back to Bookings list
                        navController.popBackStack()
                    } catch (e: Exception) {
                        error = e.message ?: "Failed to create booking"
                    } finally {
                        formBusy = false
                    }
                }
            },
            enabled = !formBusy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (formBusy) "Submitting..." else "Submit booking") }
    }
}
