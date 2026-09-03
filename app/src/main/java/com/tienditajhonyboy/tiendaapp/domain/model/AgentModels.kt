package com.tienditajhonyboy.tiendaapp.domain.model

data class ChatSession(
    val id: String,
    val workspaceId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: String, // "user" or "model"
    val content: String,
    val isApproximate: Boolean = false,
    val missingData: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AgentInsight(
    val id: String,
    val workspaceId: String,
    val title: String,
    val content: String,
    val type: InsightType,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class InsightType {
    alert, opportunity, summary
}
