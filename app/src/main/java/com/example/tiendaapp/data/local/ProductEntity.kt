package com.example.tiendaapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tiendaapp.domain.model.Product
import com.example.tiendaapp.domain.model.UnitType

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val unit: UnitType,
    val image: String,
    val createdAt: Long
)

fun ProductEntity.toDomain(): Product {
    return Product(
        id = id,
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
        name = name,
        price = price,
        unit = unit,
        image = image,
        createdAt = createdAt
    )
}
