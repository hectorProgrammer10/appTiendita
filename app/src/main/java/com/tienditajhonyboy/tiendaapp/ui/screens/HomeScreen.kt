package com.tienditajhonyboy.tiendaapp.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tienditajhonyboy.tiendaapp.ui.components.ProductCarousel
import com.tienditajhonyboy.tiendaapp.ui.viewmodel.AppViewModelProvider
import com.tienditajhonyboy.tiendaapp.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateToPOS: (String?) -> Unit,
    onNavigateToNewProduct: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    var productToDelete by remember { mutableStateOf<com.tienditajhonyboy.tiendaapp.domain.model.Product?>(null) }
    
    // Store name state with SharedPreferences
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tienda_prefs", Context.MODE_PRIVATE) }
    var storeName by remember { mutableStateOf(prefs.getString("store_name", "Tiendita") ?: "Tiendita") }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Estás seguro de que deseas eliminar '${productToDelete?.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToDelete?.let { viewModel.deleteProduct(it) }
                        productToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Edit Store Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Nombre de la Tienda") },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editingName.isNotBlank()) {
                            storeName = editingName
                            prefs.edit().putString("store_name", editingName).apply()
                        }
                        showEditNameDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(vertical = 24.dp)) {
            Text(
                text = storeName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
                modifier = Modifier.clickable {
                    editingName = storeName
                    showEditNameDialog = true
                }
            )
            Text(
                text = "Panel de Administración",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Products Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Productos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onNavigateToNewProduct) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nuevo")
            }
        }
        

        
        // Product Carousel
        ProductCarousel(
            products = uiState.productList,
            onProductClick = { product ->
                onNavigateToPOS(product.id)
            },
            onProductLongClick = { product ->
                productToDelete = product
            },
            onAddNewProduct = onNavigateToNewProduct
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Actions Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vender Button
            ActionCard(
                icon = { Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                label = "Vender",
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                labelColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPOS(null) }
            )

            // History Button
            ActionCard(
                icon = { Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF334155)) }, // Slate 700
                label = "Historial",
                backgroundColor = Color(0xFFF1F5F9), // Slate 100/200 equivalent
                labelColor = Color(0xFF334155),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToHistory
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ActionCard(
    icon: @Composable () -> Unit,
    label: String,
    backgroundColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1.33f) // 4/3 aspect ratio
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = if (backgroundColor == Color.White) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(labelColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = labelColor
            )
        }
    }
}
