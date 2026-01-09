package com.tienditajhonyboy.tiendaapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tienditajhonyboy.tiendaapp.domain.model.CartItem
import com.tienditajhonyboy.tiendaapp.domain.model.PaymentType
import com.tienditajhonyboy.tiendaapp.domain.model.Sale

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val items: List<CartItem>,
    val total: Double,
    val paymentAmount: Double,
    val change: Double,
    val paymentType: PaymentType,
    val clientName: String?,
    val date: Long
)

fun SaleEntity.toDomain(): Sale {
    return Sale(
        id = id,
        items = items,
        total = total,
        paymentAmount = paymentAmount,
        change = change,
        paymentType = paymentType,
        clientName = clientName,
        date = date
    )
}

fun Sale.toEntity(): SaleEntity {
    return SaleEntity(
        id = id,
        items = items,
        total = total,
        paymentAmount = paymentAmount,
        change = change,
        paymentType = paymentType,
        clientName = clientName,
        date = date
    )
}
