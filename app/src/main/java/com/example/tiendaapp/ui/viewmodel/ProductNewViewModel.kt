package com.example.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendaapp.domain.model.Product
import com.example.tiendaapp.domain.model.UnitType
import com.example.tiendaapp.domain.repository.ProductRepository
import kotlinx.coroutines.launch
import java.util.UUID

class ProductNewViewModel(private val productRepository: ProductRepository) : ViewModel() {

    fun saveProduct(name: String, price: Double, unit: UnitType, image: String) {
        val product = Product(
            id = UUID.randomUUID().toString(),
            name = name,
            price = price,
            unit = unit,
            image = image,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            productRepository.insertProduct(product)
        }
    }
}
