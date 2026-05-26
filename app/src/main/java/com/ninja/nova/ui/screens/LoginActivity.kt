package com.ninja.nova.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninja.nova.ui.theme.*
import com.ninja.nova.viewmodel.NovaViewModel

class LoginActivity : ComponentActivity() {
    private val viewModel: NovaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NovaTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: NovaViewModel, onLoginSuccess: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    // Orb pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "scale"
    )

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) onLoginSuccess()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(NovaDark),
        contentAlignment = Alignment.Center
    ) {
        // Background glow
        Box(
            modifier = Modifier.size(300.dp).scale(scale).blur(80.dp)
                .background(Brush.radialGradient(listOf(NovaAqua.copy(alpha = 0.15f), Color.Transparent)))
                .align(Alignment.Center)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(32.dp)
        ) {
            // Orb
            Box(
                modifier = Modifier.size(100.dp).scale(scale)
                    .background(Brush.radialGradient(listOf(NovaAqua, NovaBlue, NovaDark2)), shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("N", color = NovaDark, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))

            Text("nova", color = NovaAqua, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Text("AI Assistant", color = NovaTextDim, fontSize = 14.sp)

            Spacer(Modifier.height(48.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMsg = "" },
                label = { Text("Email", color = NovaTextDim) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaAqua,
                    unfocusedBorderColor = NovaSurface2,
                    focusedTextColor = NovaText,
                    unfocusedTextColor = NovaText,
                    cursorColor = NovaAqua
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMsg = "" },
                label = { Text("Password", color = NovaTextDim) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaAqua,
                    unfocusedBorderColor = NovaSurface2,
                    focusedTextColor = NovaText,
                    unfocusedTextColor = NovaText,
                    cursorColor = NovaAqua
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, color = NovaRed, fontSize = 13.sp)
            }

            Spacer(Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = { viewModel.login(context, email, password) { errorMsg = it } },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaAqua, contentColor = NovaDark)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NovaDark, strokeWidth = 2.dp)
                } else {
                    Text("Login", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
