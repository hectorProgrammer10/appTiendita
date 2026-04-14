package com.tienditajhonyboy.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tienditajhonyboy.tiendaapp.domain.model.Product
import com.tienditajhonyboy.tiendaapp.domain.model.UnitType
import com.tienditajhonyboy.tiendaapp.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductEditViewModel(private val productRepository: ProductRepository) : ViewModel() {

    private val _productUiState = MutableStateFlow<Product?>(null)
    val productUiState = _productUiState.asStateFlow()

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            val product = productRepository.getProductById(productId)
            _productUiState.value = product
        }
    }

    fun updateProduct(product: Product, name: String, price: Double, unit: UnitType, image: String) {
        val updatedProduct = product.copy(
            name = name,
            price = price,
            unit = unit,
            image = image
        )
        viewModelScope.launch {
            productRepository.updateProduct(updatedProduct)
        }
    }
}
