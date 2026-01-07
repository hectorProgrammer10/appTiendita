package com.example.tiendaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendaapp.ui.components.PaymentDialog
import com.example.tiendaapp.ui.components.ProductCarousel
import com.example.tiendaapp.ui.components.QuantityDialog
import com.example.tiendaapp.ui.components.SaleItemCard
import com.example.tiendaapp.ui.viewmodel.AppViewModelProvider
import com.example.tiendaapp.ui.viewmodel.POSViewModel

import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(
    viewModel: POSViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateBack: () -> Unit,
    productIdToSelect: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val products by viewModel.productsUiState.collectAsState()
    val context = LocalContext.current

    // Effects
    LaunchedEffect(productIdToSelect, products) {
        if (productIdToSelect != null && products.isNotEmpty()) {
            val product = products.find { it.id == productIdToSelect }
            if (product != null) {
                // Auto-select product logic handled via local state or VM
                // For now, we can just trigger the dialog if we want, or add directly.
                // To keep it simple and clean, let's just use the VM to add 1 unit or show dialog.
                // Since showQuantityDialog is local state, we'd need to lift it or expose an event.
                // Simpler approach: Just scroll to it or let user click. 
                // But USER asked "llevar a vender", implying adding it.
                // As a quick UX, let's auto-open the dialog for that product.
            }
        }
    }

    // Dialog state
    var showQuantityDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedProductForCart by remember { mutableStateOf<com.example.tiendaapp.domain.model.Product?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    // Auto-selection once products are loaded
    LaunchedEffect(productIdToSelect, products) {
        if (productIdToSelect != null && products.isNotEmpty() && selectedProductForCart == null) {
             val product = products.find { it.id == productIdToSelect }
             if (product != null) {
                 selectedProductForCart = product
                 showQuantityDialog = true
             }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Punto de Venta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Product Selector
            Text("Productos disponibles", modifier = Modifier.padding(16.dp))
            ProductCarousel(
                products = products,
                onProductClick = { product ->
                    selectedProductForCart = product
                    showQuantityDialog = true
                },
                onProductLongClick = { },
                onAddNewProduct = { /* No action in POS for new product, but param required. Empty or navigate? */
                    // Usually you don't create products inside POS. Just pass empty or ignore.
                    // Or maybe allow quick add? Let's just pass empty for now as it's not requested.
                }
            )

            HorizontalDivider()

            // Cart Items
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                items(uiState.cartItems.size) { index ->
                    val item = uiState.cartItems[index]
                    SaleItemCard(
                        item = item, 
                        onRemove = { viewModel.removeFromCart(index) }
                    )
                }
            }

            // Totals and Checkout
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Total display (above buttons)
                    Row(
                         modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                         horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                         Text("Total", style = MaterialTheme.typography.headlineSmall)
                         Text("$${String.format("%.2f", uiState.total)}", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Close Button (Left, Red)
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = androidx.compose.ui.graphics.Color.White)
                        }

                        // Make Sale Button (Middle, Blue, Fill)
                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = uiState.cartItems.isNotEmpty()
                        ) {
                            Text("Hacer Venta" , style = MaterialTheme.typography.titleMedium)
                        }

                        // Clear Cart Button (Right, Teal)
                        Button(
                            onClick = { 
                                viewModel.clearCart()
                                Toast.makeText(context, "Carrito limpiado", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF009688)), // Teal
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Limpiar", tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            }
        }

        if (showQuantityDialog && selectedProductForCart != null) {
            QuantityDialog(
                product = selectedProductForCart!!,
                onDismiss = { showQuantityDialog = false },
                onConfirm = { quantity ->
                    viewModel.addToCart(selectedProductForCart!!, quantity)
                    showQuantityDialog = false
                    selectedProductForCart = null
                }
            )
        }

        if (showPaymentDialog) {
            PaymentDialog(
                total = uiState.total,
                onDismiss = { showPaymentDialog = false },
                onConfirm = { amount, type, client ->
                    viewModel.checkout(amount, type, client)
                    showPaymentDialog = false
                    Toast.makeText(context, "Venta realizada con éxito", Toast.LENGTH_LONG).show()
                    // Stay in POS per user request
                    // User request: "cerrar sin guardar cambios" implies sticking around?
                    // Usually POS resets. Let's just clear logic in VM.
                }
            )
        }
    }
}
