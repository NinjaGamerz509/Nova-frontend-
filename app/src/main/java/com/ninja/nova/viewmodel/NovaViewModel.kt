package com.ninja.nova.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninja.nova.network.*
import com.ninja.nova.utils.PrefsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(val role: String, val content: String, val sources: List<Source> = emptyList(), val timestamp: Long = System.currentTimeMillis())
data class AgentStatus(val message: String = "", val isActive: Boolean = false)

class NovaViewModel : ViewModel() {
    private val api = RetrofitClient.api

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _agentStatus = MutableStateFlow(AgentStatus())
    val agentStatus: StateFlow<AgentStatus> = _agentStatus

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private var token: String? = null
    private var sessionId: String = UUID.randomUUID().toString()

    fun checkLogin(context: Context) {
        viewModelScope.launch {
            val saved = PrefsManager.getToken(context)
            if (!saved.isNullOrBlank()) {
                token = saved
                val savedSession = PrefsManager.getSession(context)
                if (!savedSession.isNullOrBlank()) sessionId = savedSession
                _isLoggedIn.value = true
            }
        }
    }

    fun login(context: Context, email: String, password: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = api.login(LoginRequest(email, password))
                if (res.isSuccessful && res.body()?.success == true) {
                    val t = res.body()!!.token!!
                    token = t
                    PrefsManager.saveToken(context, t)
                    PrefsManager.saveSession(context, sessionId)
                    _isLoggedIn.value = true
                } else {
                    onError(res.body()?.message ?: "Login failed")
                }
            } catch (e: Exception) {
                onError("Cannot connect to nova server")
            }
            _isLoading.value = false
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            PrefsManager.clearToken(context)
            token = null
            _isLoggedIn.value = false
            _messages.value = emptyList()
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _messages.value = _messages.value + ChatMessage("user", text)
            _isLoading.value = true
            _agentStatus.value = AgentStatus("nova is thinking...", true)
            try {
                val res = api.sendMessage("Bearer $token", ChatRequest(text, sessionId))
                if (res.isSuccessful && res.body()?.success == true) {
                    val body = res.body()!!
                    _messages.value = _messages.value + ChatMessage("nova", body.response, body.sources)
                } else {
                    _messages.value = _messages.value + ChatMessage("nova", "Kuch problem hui, dobara try karo.")
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("nova", "Server se connect nahi ho pa raha.")
            }
            _isLoading.value = false
            _agentStatus.value = AgentStatus("", false)
        }
    }

    fun setListening(value: Boolean) { _isListening.value = value }
    fun setSpeaking(value: Boolean) { _isSpeaking.value = value }
    fun updateAgentStatus(msg: String, active: Boolean) { _agentStatus.value = AgentStatus(msg, active) }

    fun loadTasks() {
        viewModelScope.launch {
            try {
                val res = api.getTasks("Bearer $token")
                if (res.isSuccessful) _tasks.value = res.body()?.tasks ?: emptyList()
            } catch (e: Exception) { }
        }
    }

    fun addTask(title: String, description: String = "", priority: String = "medium") {
        viewModelScope.launch {
            try {
                api.addTask("Bearer $token", TaskRequest(title, description, priority))
                loadTasks()
            } catch (e: Exception) { }
        }
    }

    fun completeTask(id: String) {
        viewModelScope.launch {
            try {
                api.completeTask("Bearer $token", id)
                loadTasks()
            } catch (e: Exception) { }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            try {
                api.deleteTask("Bearer $token", id)
                loadTasks()
            } catch (e: Exception) { }
        }
    }

    fun loadNotes() {
        viewModelScope.launch {
            try {
                val res = api.getNotes("Bearer $token")
                if (res.isSuccessful) _notes.value = res.body()?.notes ?: emptyList()
            } catch (e: Exception) { }
        }
    }

    fun loadExpenses() {
        viewModelScope.launch {
            try {
                val res = api.getExpenses("Bearer $token")
                if (res.isSuccessful) _expenses.value = res.body()?.expenses ?: emptyList()
            } catch (e: Exception) { }
        }
    }

    fun getToken(): String? = token
}
