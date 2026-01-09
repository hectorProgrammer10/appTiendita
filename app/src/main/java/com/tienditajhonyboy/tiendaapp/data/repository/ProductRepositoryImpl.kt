package com.tienditajhonyboy.tiendaapp.data.repository

import com.tienditajhonyboy.tiendaapp.data.local.ProductDao
import com.tienditajhonyboy.tiendaapp.data.local.toDomain
import com.tienditajhonyboy.tiendaapp.data.local.toEntity
import com.tienditajhonyboy.tiendaapp.domain.model.Product
import com.tienditajhonyboy.tiendaapp.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(private val dao: ProductDao) : ProductRepository {
    override fun getAllProducts(): Flow<List<Product>> {
        return dao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProductById(id: String): Product? {
        return dao.getProductById(id)?.toDomain()
    }

    override suspend fun insertProduct(product: Product) {
        dao.insertProduct(product.toEntity())
    }

    override suspend fun deleteProduct(id: String) {
        dao.deleteProduct(id)
    }

    override suspend fun updateProduct(product: Product) {
        dao.updateProduct(product.toEntity())
    }
}
