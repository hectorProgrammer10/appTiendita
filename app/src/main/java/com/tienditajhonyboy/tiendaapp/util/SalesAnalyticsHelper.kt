package com.tienditajhonyboy.tiendaapp.util

import com.tienditajhonyboy.tiendaapp.domain.model.PaymentType
import com.tienditajhonyboy.tiendaapp.domain.model.Sale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProductStat(
    val name: String,
    val quantitySold: Double,
    val unit: String,
    val totalAmount: Double,
    val transactionCount: Int
)

data class TargetProductMetrics(
    val name: String,
    val totalUnits: Double,
    val unit: String,
    val totalMoney: Double,
    val avgPrice: Double,
    val transactionCount: Int
)

data class SalesMetricsContext(
    val totalRevenue: Double,
    val totalSalesCount: Int,
    val totalContado: Double,
    val totalPendiente: Double,
    val averageTicket: Double,
    val topProducts: List<ProductStat>,
    val targetProductMetrics: TargetProductMetrics? = null
)

object SalesAnalyticsHelper {

    fun buildSalesContext(sales: List<Sale>, userQuery: String?): SalesMetricsContext {
        val nonCancelled = sales.filter { it.paymentType != PaymentType.cancelado }
        
        val totalRevenue = nonCancelled.sumOf { it.total }
        val totalContado = nonCancelled.filter { it.paymentType == PaymentType.contado }.sumOf { it.total }
        val totalPendiente = nonCancelled.filter { it.paymentType == PaymentType.pendiente }.sumOf { it.total }
        val avgTicket = if (nonCancelled.isNotEmpty()) totalRevenue / nonCancelled.size else 0.0

        // Aggregation per product
        val productMap = mutableMapOf<String, MutableProductAccumulator>()
        for (sale in nonCancelled) {
            for (item in sale.items) {
                val key = item.productName.trim()
                val acc = productMap.getOrPut(key) {
                    MutableProductAccumulator(key, item.unit.name)
                }
                acc.quantity += item.quantity
                acc.totalAmount += item.subtotal
                acc.count += 1
            }
        }

        val sortedProducts = productMap.values
            .sortedByDescending { it.totalAmount }
            .map {
                ProductStat(
                    name = it.name,
                    quantitySold = it.quantity,
                    unit = it.unit,
                    totalAmount = it.totalAmount,
                    transactionCount = it.count
                )
            }

        val top5 = sortedProducts.take(5)

        // Find target product if user mentioned a specific product in their query
        var targetProduct: TargetProductMetrics? = null
        if (!userQuery.isNullOrBlank()) {
            val queryLower = userQuery.lowercase(Locale.getDefault())
            val matchedAcc = productMap.values.firstOrNull { acc ->
                queryLower.contains(acc.name.lowercase(Locale.getDefault())) ||
                acc.name.lowercase(Locale.getDefault()).contains(queryLower)
            }
            if (matchedAcc != null) {
                val avgPrice = if (matchedAcc.quantity > 0) matchedAcc.totalAmount / matchedAcc.quantity else 0.0
                targetProduct = TargetProductMetrics(
                    name = matchedAcc.name,
                    totalUnits = matchedAcc.quantity,
                    unit = matchedAcc.unit,
                    totalMoney = matchedAcc.totalAmount,
                    avgPrice = avgPrice,
                    transactionCount = matchedAcc.count
                )
            }
        }

        return SalesMetricsContext(
            totalRevenue = totalRevenue,
            totalSalesCount = nonCancelled.size,
            totalContado = totalContado,
            totalPendiente = totalPendiente,
            averageTicket = avgTicket,
            topProducts = top5,
            targetProductMetrics = targetProduct
        )
    }

    private class MutableProductAccumulator(
        val name: String,
        val unit: String,
        var quantity: Double = 0.0,
        var totalAmount: Double = 0.0,
        var count: Int = 0
    )
}
