package com.example.skolar20.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.skolar20.R

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var info by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Listen for auth changes so UI stays in sync
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // Google client (needs default_web_client_id from google-services.json)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    // Detect sign-in provider(s)
    val providers = currentUser?.providerData?.mapNotNull { it.providerId } ?: emptyList()
    val isGoogle = providers.contains(GoogleAuthProvider.PROVIDER_ID)
    val isPassword = providers.contains("password")

    // Your previous local settings
    var lang by remember { mutableStateOf("English") }
    var notifications by remember { mutableStateOf(true) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)

        // --- Account section ---
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Account", style = MaterialTheme.typography.titleMedium)

                if (currentUser != null) {
                    Text("Signed in as: ${currentUser?.displayName ?: "(no name)"}")
                    Text("Email: ${currentUser?.email ?: "(none)"}")
                    if (providers.isNotEmpty()) {
                        Text("Provider: ${providers.joinToString()}")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {
                            // Sign out of Firebase; also sign out Google client to clear cached account
                            auth.signOut()
                            googleClient.signOut()
                            info = "Signed out."
                            error = null
                        }) { Text("Sign out") }

                        if (isGoogle) {
                            OutlinedButton(onClick = {
                                // Revoke app access from the Google account
                                googleClient.revokeAccess().addOnCompleteListener {
                                    auth.signOut()
                                    info = "Google access revoked."
                                    error = null
                                }
                            }) { Text("Revoke access") }
                        }
                    }

                    if (isPassword) {
                        OutlinedButton(onClick = {
                            val email = currentUser?.email
                            if (email.isNullOrBlank()) {
                                error = "No email on this account."
                                info = null
                            } else {
                                auth.sendPasswordResetEmail(email).addOnCompleteListener { t ->
                                    if (t.isSuccessful) {
                                        info = "Password reset email sent to $email."
                                        error = null
                                    } else {
                                        error = t.exception?.localizedMessage ?: "Could not send reset email."
                                        info = null
                                    }
                                }
                            }
                        }) { Text("Send password reset email") }
                    }
                } else {
                    Text("Not signed in")
                }

                if (info != null) Text(info!!, color = MaterialTheme.colorScheme.primary)
                if (error != null) Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }
        }

        // --- Language (kept from your screen) ---
        Text("Language")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = lang == "English", onClick = { lang = "English" }, label = { Text("English") })
            FilterChip(selected = lang == "isiZulu", onClick = { lang = "isiZulu" }, label = { Text("isiZulu") })
            FilterChip(selected = lang == "Afrikaans", onClick = { lang = "Afrikaans" }, label = { Text("Afrikaans") })
        }

        // --- Notifications (kept) ---
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Notifications")
            Switch(checked = notifications, onCheckedChange = { notifications = it })
        }

        Divider()
        // Any other settings...
    }
}
