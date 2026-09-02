package com.tienditajhonyboy.tiendaapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tienditajhonyboy.tiendaapp.domain.model.CartItem
import com.tienditajhonyboy.tiendaapp.domain.model.PaymentType
import com.tienditajhonyboy.tiendaapp.domain.model.Sale

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["workspaceId"]),
        Index(value = ["workspaceId", "date"]),
        Index(value = ["workspaceId", "paymentType"])
    ]
)
data class SaleEntity(
    @PrimaryKey val id: String,
    val workspaceId: String = "ws_default",
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
        workspaceId = workspaceId,
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
        workspaceId = workspaceId,
        items = items,
        total = total,
        paymentAmount = paymentAmount,
        change = change,
        paymentType = paymentType,
        clientName = clientName,
        date = date
    )
}
