package com.tienditajhonyboy.tiendaapp.domain.model

data class Sale(
    val id: String,
    val items: List<CartItem>,
    val total: Double,
    val paymentAmount: Double,
    val change: Double,
    val paymentType: PaymentType,
    val clientName: String? = null,
    val date: Long
)

data class CartItem(
    val productId: String,
    val productName: String,
    val unit: UnitType,
    val pricePerUnit: Double,
    val quantity: Double, // Compatible with both Int (pieces) and Double (kg)
    val subtotal: Double
)

enum class PaymentType {
    contado, pendiente, cancelado
}
