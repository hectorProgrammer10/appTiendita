package com.tienditajhonyboy.tiendaapp.domain.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val unit: UnitType,
    val image: String, // Data URL or path
    val createdAt: Long
)

enum class UnitType {
    piece, kg
}
