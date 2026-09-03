package com.tienditajhonyboy.tiendaapp.network

import android.content.Context
import com.google.gson.Gson
import com.tienditajhonyboy.tiendaapp.domain.model.ChatMessage
import com.tienditajhonyboy.tiendaapp.util.SalesMetricsContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class HistoryItemDto(
    val role: String,
    val text: String
)

data class ChatRequestDto(
    val message: String,
    val workspaceId: String,
    val history: List<HistoryItemDto>,
    val salesContext: SalesMetricsContext?
)

data class NewInsightPayloadDto(
    val title: String,
    val content: String,
    val type: String // "alert", "opportunity", "summary"
)

data class ChatResponseDto(
    val reply: String,
    val dataMissing: String? = null,
    val isApproximate: Boolean = false,
    val suggestedActions: List<String>? = null,
    val newInsight: NewInsightPayloadDto? = null
)

class AgentApiClient(private val context: Context) {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    companion object {
        const val DEFAULT_BASE_URL = "https://backendapptiendita.vercel.app"
    }

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun getBaseUrl(): String {
        val prefs = context.getSharedPreferences("agent_prefs", Context.MODE_PRIVATE)
        return prefs.getString("backend_url", DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun setBaseUrl(url: String) {
        val prefs = context.getSharedPreferences("agent_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("backend_url", url.trimEnd('/')).apply()
    }

    suspend fun sendMessage(
        message: String,
        workspaceId: String,
        recentMessages: List<ChatMessage>,
        salesContext: SalesMetricsContext?
    ): Result<ChatResponseDto> = withContext(Dispatchers.IO) {
        try {
            val history = recentMessages.takeLast(10).map {
                HistoryItemDto(
                    role = if (it.role == "user") "user" else "model",
                    text = it.content
                )
            }

            val requestDto = ChatRequestDto(
                message = message,
                workspaceId = workspaceId,
                history = history,
                salesContext = salesContext
            )

            val jsonBody = gson.toJson(requestDto)
            val requestBody = jsonBody.toRequestBody(mediaType)
            val url = "${getBaseUrl()}/api/chat"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errMsg = try {
                        val errObj = gson.fromJson(bodyString, Map::class.java)
                        errObj["error"]?.toString() ?: "Error en el servidor (${response.code})"
                    } catch (e: Exception) {
                        "Error de conexión con el servidor (${response.code})"
                    }
                    return@withContext Result.failure(Exception(errMsg))
                }

                val chatResponse = gson.fromJson(bodyString, ChatResponseDto::class.java)
                Result.success(chatResponse)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
