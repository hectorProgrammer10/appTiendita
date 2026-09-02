package com.tienditajhonyboy.tiendaapp.data.repository

import android.content.Context
import com.tienditajhonyboy.tiendaapp.data.local.WorkspaceDao
import com.tienditajhonyboy.tiendaapp.data.local.toDomain
import com.tienditajhonyboy.tiendaapp.data.local.toEntity
import com.tienditajhonyboy.tiendaapp.domain.model.Workspace
import com.tienditajhonyboy.tiendaapp.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class WorkspaceRepositoryImpl(
    private val dao: WorkspaceDao,
    private val context: Context
) : WorkspaceRepository {

    private val prefs = context.getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)

    private val _activeWorkspaceId = MutableStateFlow(
        prefs.getString("active_workspace_id", "ws_default") ?: "ws_default"
    )

    override fun getAllWorkspaces(): Flow<List<Workspace>> {
        return dao.getAllWorkspaces().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveWorkspaceId(): Flow<String> {
        return _activeWorkspaceId.asStateFlow()
    }

    override suspend fun setActiveWorkspaceId(id: String) {
        prefs.edit().putString("active_workspace_id", id).apply()
        _activeWorkspaceId.value = id
    }

    override suspend fun insertWorkspace(workspace: Workspace) {
        dao.insertWorkspace(workspace.toEntity())
    }

    override suspend fun updateWorkspaceName(id: String, newName: String) {
        dao.updateWorkspaceName(id, newName)
    }

    override suspend fun deleteWorkspace(id: String) {
        dao.deleteWorkspace(id)
    }
}
