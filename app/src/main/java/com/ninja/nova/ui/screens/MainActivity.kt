package com.ninja.nova.ui.screens

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninja.nova.services.WakeWordService
import com.ninja.nova.ui.components.*
import com.ninja.nova.ui.theme.*
import com.ninja.nova.utils.Constants
import com.ninja.nova.viewmodel.NovaViewModel
import kotlinx.coroutines.*
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: NovaViewModel by viewModels()
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var listenJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check login
        viewModel.checkLogin(this)

        // Init TTS
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }

        // Start wake word service
        if (!WakeWordService.isRunning) {
            startForegroundService(Intent(this, WakeWordService::class.java))
        }

        setContent {
            NovaTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()

                if (!isLoggedIn) {
                    LaunchedEffect(Unit) {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    MainScreen(
                        viewModel = viewModel,
                        onListen = { startListening() },
                        onStop = { stopListening() },
                        onAnswer = { text -> sendAndSpeak(text) },
                        onLogout = { viewModel.logout(this) }
                    )
                }
            }
        }
    }

    private fun startListening() {
        viewModel.setListening(true)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                viewModel.setListening(false)
                if (!text.isNullOrBlank()) {
                    viewModel.sendMessage(text)
                }
            }
            override fun onError(error: Int) { viewModel.setListening(false) }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)

        // Auto stop after 5 seconds of silence
        listenJob?.cancel()
        listenJob = CoroutineScope(Dispatchers.Main).launch {
            delay(Constants.LISTEN_TIMEOUT_MS)
            if (viewModel.isListening.value) stopListening()
        }
    }

    private fun stopListening() {
        listenJob?.cancel()
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        viewModel.setListening(false)
    }

    private fun sendAndSpeak(text: String) {
        viewModel.sendMessage(text)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }
}

@Composable
fun MainScreen(
    viewModel: NovaViewModel,
    onListen: () -> Unit,
    onStop: () -> Unit,
    onAnswer: (String) -> Unit,
    onLogout: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val agentStatus by viewModel.agentStatus.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    var showTasks by remember { mutableStateOf(false) }
    var typeInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadTasks() }

    Box(modifier = Modifier.fillMaxSize().background(NovaDark)) {
        Row(modifier = Modifier.fillMaxSize()) {

            // LEFT PANEL
            Column(
                modifier = Modifier.width(90.dp).fillMaxHeight().background(NovaSurface).padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                Text("nova", color = NovaAqua, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                // Panel buttons
                listOf("Tasks", "Notes", "Finance", "Browser", "Settings").forEach { label ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(NovaSurface2, RoundedCornerShape(8.dp))
                            .clickable {
                                when (label) {
                                    "Tasks" -> { showTasks = true; viewModel.loadTasks() }
                                    else -> {}
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = NovaTextDim, fontSize = 10.sp)
                    }
                }

                Spacer(Modifier.weight(1f))

                // Logout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NovaRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .clickable { onLogout() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Out", color = NovaRed, fontSize = 10.sp)
                }
                Spacer(Modifier.height(8.dp))
            }

            // CENTRE
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Orb
                OrbCard(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    isThinking = isLoading,
                    agentStatus = agentStatus.message,
                    modifier = Modifier.fillMaxWidth()
                )

                // Listen / Stop / Answer buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { if (isListening) onStop() else onListen() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening) NovaGreen else NovaAqua,
                            contentColor = NovaDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text(if (isListening) "Stop" else "Listen", fontSize = 13.sp, fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = { if (typeInput.isNotBlank()) { onAnswer(typeInput); typeInput = "" } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaBlue, contentColor = NovaText),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Send", fontSize = 13.sp) }
                }

                // Type input
                OutlinedTextField(
                    value = typeInput,
                    onValueChange = { typeInput = it },
                    placeholder = { Text("Type to nova...", color = NovaTextDim, fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaAqua,
                        unfocusedBorderColor = NovaSurface2,
                        focusedTextColor = NovaText,
                        unfocusedTextColor = NovaText,
                        cursorColor = NovaAqua
                    )
                )

                // Chat
                ChatCard(
                    messages = messages,
                    isLoading = isLoading,
                    modifier = Modifier.weight(1f)
                )
            }

            // RIGHT PANEL
            Column(
                modifier = Modifier.width(90.dp).fillMaxHeight().background(NovaSurface).padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                Text("Info", color = NovaAqua, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                // Agent status
                if (agentStatus.isActive) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(NovaAqua.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Text("Agent active", color = NovaAqua, fontSize = 9.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Tasks count
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(NovaSurface2, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("${tasks.count { it.status != "done" }}", color = NovaAqua, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("tasks", color = NovaTextDim, fontSize = 10.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Messages count
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(NovaSurface2, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("${messages.size}", color = NovaBlueLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("msgs", color = NovaTextDim, fontSize = 10.sp)
                    }
                }
            }
        }

        // Floating Windows
        if (showTasks) {
            TasksWindow(
                tasks = tasks,
                onAdd = { title, desc, priority -> viewModel.addTask(title, desc, priority) },
                onComplete = { viewModel.completeTask(it) },
                onDelete = { viewModel.deleteTask(it) },
                onClose = { showTasks = false },
                onMinimize = { showTasks = false }
            )
        }
    }
}
