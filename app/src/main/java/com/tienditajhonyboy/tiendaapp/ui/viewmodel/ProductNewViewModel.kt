package com.tienditajhonyboy.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tienditajhonyboy.tiendaapp.domain.model.Product
import com.tienditajhonyboy.tiendaapp.domain.model.UnitType
import com.tienditajhonyboy.tiendaapp.domain.repository.ProductRepository
import com.tienditajhonyboy.tiendaapp.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class ProductNewViewModel(
    private val productRepository: ProductRepository,
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    fun saveProduct(name: String, price: Double, unit: UnitType, image: String) {
        viewModelScope.launch {
            val activeWorkspaceId = workspaceRepository.getActiveWorkspaceId().first()

            var uniqueId: String
            do {
                uniqueId = UUID.randomUUID().toString()
            } while (productRepository.getProductById(uniqueId) != null)

            val product = Product(
                id = uniqueId,
                workspaceId = activeWorkspaceId,
                name = name,
                price = price,
                unit = unit,
                image = image,
                createdAt = System.currentTimeMillis()
            )
            productRepository.insertProduct(product)
        }
    }
}
