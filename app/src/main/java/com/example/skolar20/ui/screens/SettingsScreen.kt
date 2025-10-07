package com.example.skolar20.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var lang by remember { mutableStateOf("English") }
    var notifications by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)

        Text("Language")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = lang == "English", onClick = { lang = "English" }, label = { Text("English") })
            FilterChip(selected = lang == "isiZulu", onClick = { lang = "isiZulu" }, label = { Text("isiZulu") })
            FilterChip(selected = lang == "Afrikaans", onClick = { lang = "Afrikaans" }, label = { Text("Afrikaans") })
        }

        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Notifications")
            Switch(checked = notifications, onCheckedChange = { notifications = it })
        }

        Divider()
        Text("Authentication", style = MaterialTheme.typography.titleMedium)
        Text("Email/Google sign-in – feature to be implemented")
    }
}
