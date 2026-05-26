package com.ninja.nova.network

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val token: String?, val message: String?, val user: UserData?)
data class UserData(val email: String, val name: String)

data class ChatRequest(val message: String, val sessionId: String?)
data class ChatResponse(val success: Boolean, val response: String, val sources: List<Source>, val intent: String, val sessionId: String, val taskId: String)
data class Source(val title: String, val url: String)

data class Task(val id: String, val title: String, val description: String, val priority: String, val status: String, val deadline: String?, val addedBy: String, val tags: List<String>, val createdAt: String, val completedAt: String?)
data class TaskRequest(val title: String, val description: String? = null, val priority: String? = "medium", val deadline: String? = null, val addedBy: String? = "user", val tags: List<String>? = emptyList())
data class TasksResponse(val success: Boolean, val tasks: List<Task>)
data class TaskResponse(val success: Boolean, val task: Task)

data class Note(val id: String, val title: String, val content: String, val type: String, val isPinned: Boolean, val createdAt: String)
data class NoteRequest(val title: String? = null, val content: String, val type: String? = "text", val isPinned: Boolean? = false)
data class NotesResponse(val success: Boolean, val notes: List<Note>)

data class Expense(val id: String, val title: String, val amount: Double, val category: String, val note: String, val date: String)
data class ExpenseRequest(val title: String, val amount: Double, val category: String? = "other", val note: String? = null, val addedBy: String? = "user")
data class ExpensesResponse(val success: Boolean, val expenses: List<Expense>, val total: Double)

data class VerifyResponse(val success: Boolean, val user: UserData?)
data class GenericResponse(val success: Boolean, val message: String?)
