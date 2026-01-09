package com.tienditajhonyboy.tiendaapp.domain.repository

import com.tienditajhonyboy.tiendaapp.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): Product?
    suspend fun insertProduct(product: Product)
    suspend fun deleteProduct(id: String)
    suspend fun updateProduct(product: Product)
}
