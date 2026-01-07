package com.example.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendaapp.domain.model.CartItem
import com.example.tiendaapp.domain.model.PaymentType
import com.example.tiendaapp.domain.model.Product
import com.example.tiendaapp.domain.model.Sale
import com.example.tiendaapp.domain.repository.ProductRepository
import com.example.tiendaapp.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class POSViewModel(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {

    // Available products for selection
    val productsUiState: StateFlow<List<Product>> = productRepository.getAllProducts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = listOf()
        )

    private val _uiState = MutableStateFlow(POSUiState())
    val uiState: StateFlow<POSUiState> = _uiState.asStateFlow()

    fun addToCart(product: Product, quantity: Double) {
        val subtotal = quantity * product.price
        val newItem = CartItem(
            productId = product.id,
            productName = product.name,
            unit = product.unit,
            pricePerUnit = product.price,
            quantity = quantity,
            subtotal = subtotal
        )
        _uiState.update { currentState ->
            val updatedItems = currentState.cartItems + newItem
            currentState.copy(
                cartItems = updatedItems,
                total = updatedItems.sumOf { it.subtotal }
            )
        }
    }

    fun removeFromCart(index: Int) {
        _uiState.update { currentState ->
            val updatedItems = currentState.cartItems.toMutableList().also { it.removeAt(index) }
            currentState.copy(
                cartItems = updatedItems,
                total = updatedItems.sumOf { it.subtotal }
            )
        }
    }

    fun clearCart() {
        _uiState.update { POSUiState() }
    }

    fun checkout(paymentAmount: Double, paymentType: PaymentType, clientName: String? = null) {
        val currentState = _uiState.value
        if (currentState.cartItems.isEmpty()) return

        val sale = Sale(
            id = UUID.randomUUID().toString(),
            items = currentState.cartItems,
            total = currentState.total,
            paymentAmount = paymentAmount,
            change = kotlin.math.max(0.0, paymentAmount - currentState.total),
            paymentType = paymentType,
            clientName = clientName,
            date = System.currentTimeMillis()
        )

        viewModelScope.launch {
            saleRepository.insertSale(sale)
            clearCart()
        }
    }
}

data class POSUiState(
    val cartItems: List<CartItem> = listOf(),
    val total: Double = 0.0
)
