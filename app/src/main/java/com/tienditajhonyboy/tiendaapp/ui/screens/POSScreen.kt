package com.tienditajhonyboy.tiendaapp.ui.screens

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
import com.tienditajhonyboy.tiendaapp.ui.components.PaymentDialog
import com.tienditajhonyboy.tiendaapp.ui.components.ProductCarousel
import com.tienditajhonyboy.tiendaapp.ui.components.QuantityDialog
import com.tienditajhonyboy.tiendaapp.ui.components.SaleItemCard
import com.tienditajhonyboy.tiendaapp.ui.viewmodel.AppViewModelProvider
import com.tienditajhonyboy.tiendaapp.ui.viewmodel.POSViewModel

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var selectedProductForCart by remember { mutableStateOf<com.tienditajhonyboy.tiendaapp.domain.model.Product?>(null) }
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(
                text = "Productos disponibles",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            ProductCarousel(
                products = products,
                onProductClick = { product ->
                    selectedProductForCart = product
                    showQuantityDialog = true
                },
                onProductLongClick = { },
                onAddNewProduct = { }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()

            if (uiState.cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "El carrito está vacío.\nSelecciona un producto para agregarlo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(
                        items = uiState.cartItems,
                        key = { index, item -> "${item.productId}_$index" }
                    ) { index, item ->
                        SaleItemCard(
                            item = item, 
                            onRemove = { viewModel.removeFromCart(index) }
                        )
                    }
                }
            }

            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                         modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically
                    ) {
                         Text("Total", style = MaterialTheme.typography.headlineSmall)
                         Text("$${String.format("%.2f", uiState.total)}", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = androidx.compose.ui.graphics.Color.White)
                        }

                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = uiState.cartItems.isNotEmpty()
                        ) {
                            Text("Hacer Venta" , style = MaterialTheme.typography.titleMedium)
                        }

                        Button(
                            onClick = { 
                                viewModel.clearCart()
                                Toast.makeText(context, "Carrito limpiado", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(50.dp),
                            enabled = uiState.cartItems.isNotEmpty()
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

                }
            )
        }
    }
}
