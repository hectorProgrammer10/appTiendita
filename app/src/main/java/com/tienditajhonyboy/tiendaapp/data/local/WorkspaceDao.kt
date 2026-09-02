package com.tienditajhonyboy.tiendaapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspaces ORDER BY createdAt ASC")
    fun getAllWorkspaces(): Flow<List<WorkspaceEntity>>

    @Query("SELECT * FROM workspaces WHERE id = :id")
    suspend fun getWorkspaceById(id: String): WorkspaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspace(workspace: WorkspaceEntity)

    @Query("UPDATE workspaces SET name = :newName WHERE id = :id")
    suspend fun updateWorkspaceName(id: String, newName: String)

    @Query("DELETE FROM workspaces WHERE id = :id")
    suspend fun deleteWorkspace(id: String)
}
