package com.tienditajhonyboy.tiendaapp.data.local.agent

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tienditajhonyboy.tiendaapp.domain.model.AgentInsight
import com.tienditajhonyboy.tiendaapp.domain.model.ChatMessage
import com.tienditajhonyboy.tiendaapp.domain.model.ChatSession
import com.tienditajhonyboy.tiendaapp.domain.model.InsightType

@Entity(
    tableName = "chat_sessions",
    indices = [
        Index(value = ["workspaceId"]),
        Index(value = ["updatedAt"])
    ]
)
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

fun ChatSessionEntity.toDomain() = ChatSession(
    id = id,
    workspaceId = workspaceId,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ChatSession.toEntity() = ChatSessionEntity(
    id = id,
    workspaceId = workspaceId,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt
)

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["timestamp"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: String,
    val content: String,
    val isApproximate: Boolean = false,
    val missingData: String? = null,
    val timestamp: Long
)

fun ChatMessageEntity.toDomain() = ChatMessage(
    id = id,
    sessionId = sessionId,
    role = role,
    content = content,
    isApproximate = isApproximate,
    missingData = missingData,
    timestamp = timestamp
)

fun ChatMessage.toEntity() = ChatMessageEntity(
    id = id,
    sessionId = sessionId,
    role = role,
    content = content,
    isApproximate = isApproximate,
    missingData = missingData,
    timestamp = timestamp
)

@Entity(
    tableName = "agent_insights",
    indices = [
        Index(value = ["workspaceId"]),
        Index(value = ["createdAt"]),
        Index(value = ["isRead"])
    ]
)
data class AgentInsightEntity(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val title: String,
    val content: String,
    val type: InsightType,
    val isRead: Boolean,
    val createdAt: Long
)

fun AgentInsightEntity.toDomain() = AgentInsight(
    id = id,
    workspaceId = workspaceId,
    title = title,
    content = content,
    type = type,
    isRead = isRead,
    createdAt = createdAt
)

fun AgentInsight.toEntity() = AgentInsightEntity(
    id = id,
    workspaceId = workspaceId,
    title = title,
    content = content,
    type = type,
    isRead = isRead,
    createdAt = createdAt
)
