package com.tienditajhonyboy.tiendaapp.domain.repository

import com.tienditajhonyboy.tiendaapp.domain.model.Workspace
import kotlinx.coroutines.flow.Flow

interface WorkspaceRepository {
    fun getAllWorkspaces(): Flow<List<Workspace>>
    fun getActiveWorkspaceId(): Flow<String>
    suspend fun setActiveWorkspaceId(id: String)
    suspend fun insertWorkspace(workspace: Workspace)
    suspend fun updateWorkspaceName(id: String, newName: String)
    suspend fun deleteWorkspace(id: String)
}
