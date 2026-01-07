package com.example.tiendaapp.data.repository

import com.example.tiendaapp.data.local.ProductDao
import com.example.tiendaapp.data.local.toDomain
import com.example.tiendaapp.data.local.toEntity
import com.example.tiendaapp.domain.model.Product
import com.example.tiendaapp.domain.repository.ProductRepository
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
