package com.tienditajhonyboy.tiendaapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tienditajhonyboy.tiendaapp.domain.model.Workspace

@Entity(tableName = "workspaces")
data class WorkspaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long
)

fun WorkspaceEntity.toDomain(): Workspace {
    return Workspace(
        id = id,
        name = name,
        createdAt = createdAt
    )
}

fun Workspace.toEntity(): WorkspaceEntity {
    return WorkspaceEntity(
        id = id,
        name = name,
        createdAt = createdAt
    )
}
