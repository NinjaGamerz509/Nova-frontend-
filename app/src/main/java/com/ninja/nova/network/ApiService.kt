package com.ninja.nova.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/auth/verify")
    suspend fun verify(@Header("Authorization") token: String): Response<VerifyResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<GenericResponse>

    @POST("api/chat/message")
    suspend fun sendMessage(@Header("Authorization") token: String, @Body request: ChatRequest): Response<ChatResponse>

    @GET("api/chat/history/{sessionId}")
    suspend fun getHistory(@Header("Authorization") token: String, @Path("sessionId") sessionId: String): Response<GenericResponse>

    @DELETE("api/chat/history/{sessionId}")
    suspend fun clearHistory(@Header("Authorization") token: String, @Path("sessionId") sessionId: String): Response<GenericResponse>

    @Multipart
    @POST("api/voice/stt")
    suspend fun speechToText(@Header("Authorization") token: String, @Part audio: MultipartBody.Part): Response<GenericResponse>

    @GET("api/tasks")
    suspend fun getTasks(@Header("Authorization") token: String): Response<TasksResponse>

    @POST("api/tasks")
    suspend fun addTask(@Header("Authorization") token: String, @Body request: TaskRequest): Response<TaskResponse>

    @PATCH("api/tasks/{id}/complete")
    suspend fun completeTask(@Header("Authorization") token: String, @Path("id") id: String): Response<TaskResponse>

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Header("Authorization") token: String, @Path("id") id: String): Response<GenericResponse>

    @GET("api/notes")
    suspend fun getNotes(@Header("Authorization") token: String): Response<NotesResponse>

    @POST("api/notes")
    suspend fun addNote(@Header("Authorization") token: String, @Body request: NoteRequest): Response<GenericResponse>

    @DELETE("api/notes/{id}")
    suspend fun deleteNote(@Header("Authorization") token: String, @Path("id") id: String): Response<GenericResponse>

    @GET("api/finance/expenses")
    suspend fun getExpenses(@Header("Authorization") token: String): Response<ExpensesResponse>

    @POST("api/finance/expenses")
    suspend fun addExpense(@Header("Authorization") token: String, @Body request: ExpenseRequest): Response<GenericResponse>
}
