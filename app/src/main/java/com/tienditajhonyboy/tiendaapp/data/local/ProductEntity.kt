package com.tienditajhonyboy.tiendaapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tienditajhonyboy.tiendaapp.domain.model.Product
import com.tienditajhonyboy.tiendaapp.domain.model.UnitType

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["workspaceId"]),
        Index(value = ["workspaceId", "createdAt"])
    ]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val workspaceId: String = "ws_default",
    val name: String,
    val price: Double,
    val unit: UnitType,
    val image: String,
    val createdAt: Long
)

fun ProductEntity.toDomain(): Product {
    return Product(
        id = id,
        workspaceId = workspaceId,
        name = name,
        price = price,
        unit = unit,
        image = image,
        createdAt = createdAt
    )
}

fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        workspaceId = workspaceId,
        name = name,
        price = price,
        unit = unit,
        image = image,
        createdAt = createdAt
    )
}
