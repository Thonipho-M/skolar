package com.example.skolar20.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skolar20.data.model.remote.*
import com.example.skolar20.data.model.Tutor

@Composable
fun TutorsScreen() {
    var tutors by remember { mutableStateOf<List<Tutor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            tutors = FirestoreService.fetchTutors()
        } catch (e: Exception) {
            error = e.message ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tutors", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        if (isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else if (error != null) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tutors) { tutor -> TutorCard(tutor) }
            }
        }
    }
}

@Composable
private fun TutorCard(tutor: Tutor) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tutor.name, style = MaterialTheme.typography.titleMedium)
            Text("Expertise: ${tutor.expertise.joinToString()}")
            Text("Qualifications: ${tutor.qualifications}")
            Text("Rate: R${tutor.rate}/hr")
            Text("Location: ${tutor.location}")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { /* TODO booking flow */ }) { Text("Request Session") }
                OutlinedButton(onClick = { /* TODO open profile */ }) { Text("View Profile") }
            }
        }
    }
}
