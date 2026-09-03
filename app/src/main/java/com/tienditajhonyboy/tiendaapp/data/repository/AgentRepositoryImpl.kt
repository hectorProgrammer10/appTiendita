package com.tienditajhonyboy.tiendaapp.data.repository

import com.tienditajhonyboy.tiendaapp.data.local.agent.AgentDao
import com.tienditajhonyboy.tiendaapp.data.local.agent.toDomain
import com.tienditajhonyboy.tiendaapp.data.local.agent.toEntity
import com.tienditajhonyboy.tiendaapp.domain.model.AgentInsight
import com.tienditajhonyboy.tiendaapp.domain.model.ChatMessage
import com.tienditajhonyboy.tiendaapp.domain.model.ChatSession
import com.tienditajhonyboy.tiendaapp.domain.repository.AgentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AgentRepositoryImpl(private val dao: AgentDao) : AgentRepository {

    override fun getSessionsByWorkspace(workspaceId: String): Flow<List<ChatSession>> {
        return dao.getSessionsByWorkspace(workspaceId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getSessionById(sessionId: String): ChatSession? {
        return dao.getSessionById(sessionId)?.toDomain()
    }

    override suspend fun insertSession(session: ChatSession) {
        dao.insertSession(session.toEntity())
    }

    override suspend fun updateSessionTimestamp(sessionId: String) {
        dao.updateSessionTimestamp(sessionId)
    }

    override suspend fun deleteSession(sessionId: String) {
        dao.deleteMessagesBySession(sessionId)
        dao.deleteSession(sessionId)
    }

    override fun getMessagesBySession(sessionId: String): Flow<List<ChatMessage>> {
        return dao.getMessagesBySession(sessionId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertMessage(message: ChatMessage) {
        dao.insertMessage(message.toEntity())
    }

    override suspend fun deleteMessagesBySession(sessionId: String) {
        dao.deleteMessagesBySession(sessionId)
    }

    override fun getInsightsByWorkspace(workspaceId: String): Flow<List<AgentInsight>> {
        return dao.getInsightsByWorkspace(workspaceId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertInsight(insight: AgentInsight) {
        dao.insertInsight(insight.toEntity())
    }

    override suspend fun markInsightAsRead(insightId: String) {
        dao.markInsightAsRead(insightId)
    }

    override suspend fun deleteInsight(insightId: String) {
        dao.deleteInsight(insightId)
    }
}
