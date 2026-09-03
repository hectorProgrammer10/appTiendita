package com.tienditajhonyboy.tiendaapp.data.local.agent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {

    // --- Chat Sessions ---
    @Query("SELECT * FROM chat_sessions WHERE workspaceId = :workspaceId ORDER BY updatedAt DESC")
    fun getSessionsByWorkspace(workspaceId: String): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Query("UPDATE chat_sessions SET updatedAt = :timestamp WHERE id = :sessionId")
    suspend fun updateSessionTimestamp(sessionId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySession(sessionId: String)

    // --- Agent Insights ---
    @Query("SELECT * FROM agent_insights WHERE workspaceId = :workspaceId ORDER BY createdAt DESC")
    fun getInsightsByWorkspace(workspaceId: String): Flow<List<AgentInsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: AgentInsightEntity)

    @Query("UPDATE agent_insights SET isRead = 1 WHERE id = :insightId")
    suspend fun markInsightAsRead(insightId: String)

    @Query("DELETE FROM agent_insights WHERE id = :insightId")
    suspend fun deleteInsight(insightId: String)
}
