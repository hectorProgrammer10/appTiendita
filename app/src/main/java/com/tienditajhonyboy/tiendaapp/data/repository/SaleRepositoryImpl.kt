package com.tienditajhonyboy.tiendaapp.data.repository

import com.tienditajhonyboy.tiendaapp.data.local.SaleDao
import com.tienditajhonyboy.tiendaapp.data.local.toDomain
import com.tienditajhonyboy.tiendaapp.data.local.toEntity
import com.tienditajhonyboy.tiendaapp.domain.model.PaymentType
import com.tienditajhonyboy.tiendaapp.domain.model.Sale
import com.tienditajhonyboy.tiendaapp.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SaleRepositoryImpl(private val dao: SaleDao) : SaleRepository {
    override fun getAllSales(): Flow<List<Sale>> {
        return dao.getAllSales().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSalesByWorkspace(workspaceId: String): Flow<List<Sale>> {
        return dao.getSalesByWorkspace(workspaceId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSaleById(id: String): Sale? {
        return dao.getSaleById(id)?.toDomain()
    }

    override suspend fun insertSale(sale: Sale) {
        dao.insertSale(sale.toEntity())
    }

    override suspend fun updateSaleStatus(id: String, status: PaymentType) {
        dao.updateSaleStatus(id, status)
    }

    override suspend fun deleteSale(id: String) {
        dao.deleteSale(id)
    }

    override suspend fun deleteAllSales() {
        dao.deleteAllSales()
    }

    override suspend fun deleteSalesByWorkspace(workspaceId: String) {
        dao.deleteSalesByWorkspace(workspaceId)
    }

    override suspend fun deleteSalesByWorkspaceAndType(workspaceId: String, paymentType: PaymentType) {
        dao.deleteSalesByWorkspaceAndType(workspaceId, paymentType)
    }
}
