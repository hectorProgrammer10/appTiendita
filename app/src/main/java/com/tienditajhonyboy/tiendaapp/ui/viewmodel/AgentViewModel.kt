package com.tienditajhonyboy.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tienditajhonyboy.tiendaapp.domain.model.AgentInsight
import com.tienditajhonyboy.tiendaapp.domain.model.ChatMessage
import com.tienditajhonyboy.tiendaapp.domain.model.ChatSession
import com.tienditajhonyboy.tiendaapp.domain.model.InsightType
import com.tienditajhonyboy.tiendaapp.domain.repository.AgentRepository
import com.tienditajhonyboy.tiendaapp.domain.repository.SaleRepository
import com.tienditajhonyboy.tiendaapp.domain.repository.WorkspaceRepository
import com.tienditajhonyboy.tiendaapp.network.AgentApiClient
import com.tienditajhonyboy.tiendaapp.util.SalesAnalyticsHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AgentViewModel(
    private val agentRepository: AgentRepository,
    private val saleRepository: SaleRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val apiClient: AgentApiClient
) : ViewModel() {

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId = _currentSessionId.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _suggestedActions = MutableStateFlow<List<String>>(
        listOf(
            "¿Cuál es el producto más vendido?",
            "¿Cuánto dinero hay pendiente de cobro?",
            "Dame un resumen de ventas",
            "¿Qué producto genera más ingresos?"
        )
    )
    val suggestedActions = _suggestedActions.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val sessions: StateFlow<List<ChatSession>> = workspaceRepository.getActiveWorkspaceId()
        .flatMapLatest { wsId -> agentRepository.getSessionsByWorkspace(wsId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> = _currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId == null) flowOf(emptyList())
            else agentRepository.getMessagesBySession(sessionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val insights: StateFlow<List<AgentInsight>> = workspaceRepository.getActiveWorkspaceId()
        .flatMapLatest { wsId -> agentRepository.getInsightsByWorkspace(wsId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Automatically select or create session for current workspace
        viewModelScope.launch {
            val wsId = workspaceRepository.getActiveWorkspaceId().first()
            val existing = agentRepository.getSessionsByWorkspace(wsId).first()
            if (existing.isNotEmpty()) {
                _currentSessionId.value = existing.first().id
            } else {
                createNewSession()
            }
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun createNewSession() {
        viewModelScope.launch {
            val wsId = workspaceRepository.getActiveWorkspaceId().first()
            val newId = UUID.randomUUID().toString()
            val newSession = ChatSession(
                id = newId,
                workspaceId = wsId,
                title = "Consulta ${System.currentTimeMillis() % 10000}",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            agentRepository.insertSession(newSession)
            _currentSessionId.value = newId
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            var sessionId = _currentSessionId.value
            val wsId = workspaceRepository.getActiveWorkspaceId().first()

            if (sessionId == null) {
                sessionId = UUID.randomUUID().toString()
                agentRepository.insertSession(
                    ChatSession(
                        id = sessionId,
                        workspaceId = wsId,
                        title = trimmed.take(25),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                _currentSessionId.value = sessionId
            }

            // 1. Insert user message
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "user",
                content = trimmed,
                timestamp = System.currentTimeMillis()
            )
            agentRepository.insertMessage(userMsg)
            agentRepository.updateSessionTimestamp(sessionId)

            _isGenerating.value = true
            _errorMessage.value = null

            // 2. Fetch sales from Room and calculate deterministic metrics
            val sales = saleRepository.getSalesByWorkspace(wsId).first()
            val salesContext = SalesAnalyticsHelper.buildSalesContext(sales, trimmed)

            // 3. Call backend Gemini Flash Lite API
            val currentRecentMessages = agentRepository.getMessagesBySession(sessionId).first()
            val result = apiClient.sendMessage(
                message = trimmed,
                workspaceId = wsId,
                recentMessages = currentRecentMessages,
                salesContext = salesContext
            )

            result.onSuccess { response ->
                _isGenerating.value = false

                // 4. Insert model response
                val modelMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = "model",
                    content = response.reply,
                    isApproximate = response.isApproximate,
                    missingData = response.dataMissing,
                    timestamp = System.currentTimeMillis()
                )
                agentRepository.insertMessage(modelMsg)

                // 5. Update suggested actions if any
                if (!response.suggestedActions.isNullOrEmpty()) {
                    _suggestedActions.value = response.suggestedActions
                }

                // 6. Insert new insight if generated by the agent
                if (response.newInsight != null) {
                    val insightType = try {
                        InsightType.valueOf(response.newInsight.type.lowercase())
                    } catch (e: Exception) {
                        InsightType.summary
                    }
                    val insight = AgentInsight(
                        id = UUID.randomUUID().toString(),
                        workspaceId = wsId,
                        title = response.newInsight.title,
                        content = response.newInsight.content,
                        type = insightType,
                        isRead = false,
                        createdAt = System.currentTimeMillis()
                    )
                    agentRepository.insertInsight(insight)
                }
            }.onFailure { exception ->
                _isGenerating.value = false
                _errorMessage.value = exception.message ?: "Error al conectar con el Asistente IA"
            }
        }
    }

    fun markInsightAsRead(insightId: String) {
        viewModelScope.launch {
            agentRepository.markInsightAsRead(insightId)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            agentRepository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                createNewSession()
            }
        }
    }

    fun getBackendUrl(): String = apiClient.getBaseUrl()

    fun updateBackendUrl(url: String) {
        apiClient.setBaseUrl(url)
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
