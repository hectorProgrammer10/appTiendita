package com.tienditajhonyboy.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tienditajhonyboy.tiendaapp.domain.model.Product
import com.tienditajhonyboy.tiendaapp.domain.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val productsRepository: ProductRepository) : ViewModel() {
    val homeUiState: StateFlow<HomeUiState> = productsRepository.getAllProducts()
        .map { HomeUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productsRepository.deleteProduct(product.id)
        }
    }
}

data class HomeUiState(val productList: List<Product> = listOf())


