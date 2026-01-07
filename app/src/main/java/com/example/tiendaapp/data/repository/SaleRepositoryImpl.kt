package com.example.tiendaapp.data.repository

import com.example.tiendaapp.data.local.SaleDao
import com.example.tiendaapp.data.local.toDomain
import com.example.tiendaapp.data.local.toEntity
import com.example.tiendaapp.domain.model.PaymentType
import com.example.tiendaapp.domain.model.Sale
import com.example.tiendaapp.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SaleRepositoryImpl(private val dao: SaleDao) : SaleRepository {
    override fun getAllSales(): Flow<List<Sale>> {
        return dao.getAllSales().map { entities ->
            entities.map { it.toDomain() }
        }
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
}
