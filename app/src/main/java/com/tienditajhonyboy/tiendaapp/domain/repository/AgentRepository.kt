package com.tienditajhonyboy.tiendaapp.domain.repository

import com.tienditajhonyboy.tiendaapp.domain.model.AgentInsight
import com.tienditajhonyboy.tiendaapp.domain.model.ChatMessage
import com.tienditajhonyboy.tiendaapp.domain.model.ChatSession
import kotlinx.coroutines.flow.Flow

interface AgentRepository {
    fun getSessionsByWorkspace(workspaceId: String): Flow<List<ChatSession>>
    suspend fun getSessionById(sessionId: String): ChatSession?
    suspend fun insertSession(session: ChatSession)
    suspend fun updateSessionTimestamp(sessionId: String)
    suspend fun deleteSession(sessionId: String)

    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessage>>
    suspend fun insertMessage(message: ChatMessage)
    suspend fun deleteMessagesBySession(sessionId: String)

    fun getInsightsByWorkspace(workspaceId: String): Flow<List<AgentInsight>>
    suspend fun insertInsight(insight: AgentInsight)
    suspend fun markInsightAsRead(insightId: String)
    suspend fun deleteInsight(insightId: String)
}
