package com.example.tiendaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

    var showQuantityDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedProductForCart by remember { mutableStateOf<com.example.tiendaapp.domain.model.Product?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(productIdToSelect, products) {
        if (productIdToSelect != null && products.isNotEmpty()) {
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
            Text("Productos disponibles", modifier = Modifier.padding(16.dp))
            ProductCarousel(
                products = products,
                onProductClick = { product ->
                    selectedProductForCart = product
                    showQuantityDialog = true
                },
                onProductLongClick = { },
                onAddNewProduct = { }
            )

            HorizontalDivider()

            // Cart Items
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                itemsIndexed(
                    items = uiState.cartItems,
                    key = { index, item -> "${item.productId}_${index}" }
                ) { index, item ->
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
