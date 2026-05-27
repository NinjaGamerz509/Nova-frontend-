package com.ninja.nova.ui.screens

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninja.nova.services.PhoneActionService
import com.ninja.nova.services.WakeWordService
import com.ninja.nova.ui.components.*
import com.ninja.nova.ui.theme.*
import com.ninja.nova.utils.Constants
import com.ninja.nova.viewmodel.NovaViewModel
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: NovaViewModel by viewModels()
    private var speechRecognizer: SpeechRecognizer? = null
    private var listenJob: Job? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ttsReady = true
            }
        }

        if (!WakeWordService.isRunning) {
            startForegroundService(Intent(this, WakeWordService::class.java))
        }

        setContent {
            NovaTheme {
                val messages by viewModel.messages.collectAsState()
                LaunchedEffect(messages.size) {
                    if (messages.isNotEmpty()) {
                        val last = messages.last()
                        if (last.role == "nova") {
                            // Execute phone actions if any
                            executePhoneAction(last.content)
                            // TTS
                            if (ttsReady) {
                                val cleanText = last.content.replace(Regex("<action>[\\s\\S]*?</action>"), "").trim()
                                tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
                                viewModel.setSpeaking(true)
                            }
                        }
                    }
                }
                MainScreen(
                    viewModel = viewModel,
                    onListen = { startListening() },
                    onStop = { stopListening() },
                    onAnswer = { text -> viewModel.sendMessage(text) }
                )
            }
        }
    }

    private fun executePhoneAction(response: String) {
        try {
            val actionMatch = Regex("<action>([\\s\\S]*?)</action>").find(response) ?: return
            val actionJson = JSONObject(actionMatch.groupValues[1].trim())
            val type = actionJson.getString("type")
            val params = actionJson.optJSONObject("params")

            when (type) {
                "phone_action" -> {
                    val action = params?.optString("action") ?: return
                    when (action) {
                        "bluetooth_on" -> PhoneActionService.setBluetoothOn(this, true)
                        "bluetooth_off" -> PhoneActionService.setBluetoothOn(this, false)
                        "wifi_on" -> PhoneActionService.setWifiOn(this, true)
                        "wifi_off" -> PhoneActionService.setWifiOn(this, false)
                        "flashlight_on" -> PhoneActionService.setFlashlight(this, true)
                        "flashlight_off" -> PhoneActionService.setFlashlight(this, false)
                        "volume" -> PhoneActionService.setVolume(this, params.optInt("level", 50))
                        "open_settings" -> PhoneActionService.openSettings(this)
                    }
                }
                "open_app" -> {
                    val appName = params?.optString("app") ?: return
                    val pkg = PhoneActionService.APP_PACKAGES[appName.lowercase()]
                    if (pkg != null) PhoneActionService.openApp(this, pkg)
                }
                "spotify" -> {
                    val query = params?.optString("query") ?: ""
                    PhoneActionService.openSpotify(this, query)
                }
                "youtube" -> {
                    val query = params?.optString("query") ?: ""
                    PhoneActionService.openYouTube(this, query)
                }
                "phone_call" -> {
                    val number = params?.optString("number") ?: return
                    PhoneActionService.makeCall(this, number)
                }
                "phone_message" -> {
                    val number = params?.optString("number") ?: return
                    val message = params.optString("message") ?: ""
                    PhoneActionService.sendWhatsApp(this, number, message)
                }
                "web_search", "fetch_news" -> {
                    val query = params?.optString("query") ?: return
                    PhoneActionService.openUrl(this, "https://www.google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}")
                }
            }
        } catch (e: Exception) { }
    }

    private fun startListening() {
        tts?.stop()
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
                if (!text.isNullOrBlank()) viewModel.sendMessage(text)
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
    onAnswer: (String) -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val agentStatus by viewModel.agentStatus.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    var showTasks by remember { mutableStateOf(false) }
    var showNotes by remember { mutableStateOf(false) }
    var showFinance by remember { mutableStateOf(false) }
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
                listOf("Tasks", "Notes", "Finance", "Browser", "Settings").forEach { label ->
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .background(NovaSurface2, RoundedCornerShape(8.dp))
                            .clickable {
                                when(label) {
                                    "Tasks" -> { showTasks = true; viewModel.loadTasks() }
                                    "Notes" -> { showNotes = true; viewModel.loadNotes() }
                                    "Finance" -> { showFinance = true; viewModel.loadExpenses() }
                                    "Browser" -> viewModel.sendMessage("search karo latest news")
                                    "Settings" -> viewModel.sendMessage("settings open karo")
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(label, color = NovaTextDim, fontSize = 10.sp) }
                }
            }

            // CENTRE
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrbCard(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    isThinking = isLoading,
                    agentStatus = agentStatus.message,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (isListening) onStop() else onListen() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isListening) NovaGreen else NovaAqua, contentColor = NovaDark),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text(if (isListening) "Stop" else "Listen", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { if (typeInput.isNotBlank()) { onAnswer(typeInput); typeInput = "" } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaBlue, contentColor = NovaText),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Send", fontSize = 13.sp) }
                }
                OutlinedTextField(
                    value = typeInput,
                    onValueChange = { typeInput = it },
                    placeholder = { Text("Type to nova...", color = NovaTextDim, fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NovaAqua, unfocusedBorderColor = NovaSurface2, focusedTextColor = NovaText, unfocusedTextColor = NovaText, cursorColor = NovaAqua)
                )
                ChatCard(messages = messages, isLoading = isLoading, modifier = Modifier.weight(1f))
            }

            // RIGHT PANEL
            Column(
                modifier = Modifier.width(90.dp).fillMaxHeight().background(NovaSurface).padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                Text("Info", color = NovaAqua, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                if (agentStatus.isActive) {
                    Box(modifier = Modifier.fillMaxWidth().background(NovaAqua.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(6.dp)) {
                        Text("Agent active", color = NovaAqua, fontSize = 9.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Box(modifier = Modifier.fillMaxWidth().background(NovaSurface2, RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("${tasks.count { it.status != "done" }}", color = NovaAqua, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("tasks", color = NovaTextDim, fontSize = 10.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().background(NovaSurface2, RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("${messages.size}", color = NovaBlueLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("msgs", color = NovaTextDim, fontSize = 10.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().background(NovaSurface2, RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("${expenses.size}", color = NovaYellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("spent", color = NovaTextDim, fontSize = 10.sp)
                    }
                }
            }
        }

        if (showTasks) {
            TasksWindow(tasks = tasks, onAdd = { title, desc, priority -> viewModel.addTask(title, desc, priority) },
                onComplete = { viewModel.completeTask(it) }, onDelete = { viewModel.deleteTask(it) },
                onClose = { showTasks = false }, onMinimize = { showTasks = false })
        }
        if (showNotes) {
            NotesWindow(notes = notes, onAdd = { title, content -> viewModel.addNote(title, content) },
                onDelete = { viewModel.deleteNote(it) }, onClose = { showNotes = false }, onMinimize = { showNotes = false })
        }
        if (showFinance) {
            FinanceWindow(expenses = expenses, onAdd = { title, amount, category -> viewModel.addExpense(title, amount, category) },
                onDelete = { viewModel.deleteExpense(it) }, onClose = { showFinance = false }, onMinimize = { showFinance = false })
        }
    }
}
