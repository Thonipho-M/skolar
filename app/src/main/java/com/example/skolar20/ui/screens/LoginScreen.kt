package com.example.skolar20.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skolar20.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    // Form state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signingUp by remember { mutableStateOf(false) } // false = login, true = create account
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Google SSO
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            busy = true
            auth.signInWithCredential(credential).addOnCompleteListener { t ->
                busy = false
                error = if (t.isSuccessful) null else t.exception?.localizedMessage ?: "Sign-in failed"
            }
        } catch (e: ApiException) {
            error = e.localizedMessage ?: "Google sign-in error"
        }
    }

    fun validateEmailPassword(): String? {
        if (!email.contains("@") || !email.contains(".")) return "Enter a valid email"
        if (password.length < 6) return "Password must be at least 6 characters"
        return null
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Skolar logo",
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(8.dp))

            Text("Welcome to Skolar", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(if (signingUp) "Create your account" else "Sign in to continue")

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            // Email/Password action
            Button(
                onClick = {
                    val err = validateEmailPassword()
                    if (err != null) { error = err; return@Button }
                    error = null
                    busy = true
                    if (signingUp) {
                        auth.createUserWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener { t ->
                                busy = false
                                error = if (t.isSuccessful) null else t.exception?.localizedMessage ?: "Sign-up failed"
                            }
                    } else {
                        auth.signInWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener { t ->
                                busy = false
                                error = if (t.isSuccessful) null else t.exception?.localizedMessage ?: "Sign-in failed"
                            }
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (signingUp) stringResource(id = R.string.create_account)
                    else stringResource(id = R.string.sign_in)
                )
            }

            // Toggle login/signup
            TextButton(onClick = { signingUp = !signingUp }) {
                Text(
                    if (signingUp) stringResource(id = R.string.toggle_have_account)
                    else stringResource(id = R.string.toggle_login_signup)
                )
            }

            // Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(Modifier.weight(1f))
                Text("or")
                Divider(Modifier.weight(1f))
            }

            // Google Sign-In
            OutlinedButton(
                onClick = { launcher.launch(googleClient.signInIntent) },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(id = R.string.sign_in_google)) }

            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }
        }

        if (busy) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}
