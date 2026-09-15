package com.tienditajhonyboy.tiendaapp.domain.repository

import com.tienditajhonyboy.tiendaapp.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    fun getAllSales(): Flow<List<Sale>>
    fun getSalesByWorkspace(workspaceId: String): Flow<List<Sale>>
    suspend fun getSaleById(id: String): Sale?
    suspend fun insertSale(sale: Sale)
    suspend fun updateSaleStatus(id: String, status: com.tienditajhonyboy.tiendaapp.domain.model.PaymentType)
    suspend fun deleteSale(id: String)
    suspend fun deleteAllSales()
    suspend fun deleteSalesByWorkspace(workspaceId: String)
    suspend fun deleteSalesByWorkspaceAndType(workspaceId: String, paymentType: com.tienditajhonyboy.tiendaapp.domain.model.PaymentType)
}


