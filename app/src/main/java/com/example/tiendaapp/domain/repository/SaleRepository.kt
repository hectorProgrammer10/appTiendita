package com.example.tiendaapp.domain.repository

import com.example.tiendaapp.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    fun getAllSales(): Flow<List<Sale>>
    suspend fun insertSale(sale: Sale)
    suspend fun updateSaleStatus(id: String, status: com.example.tiendaapp.domain.model.PaymentType)
    suspend fun deleteSale(id: String)
    suspend fun deleteAllSales()
}
