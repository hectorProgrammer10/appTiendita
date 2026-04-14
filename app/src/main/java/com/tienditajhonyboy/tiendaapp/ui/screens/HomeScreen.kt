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
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.BrowserUpdated
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.tienditajhonyboy.tiendaapp.ui.theme.Slate100
import com.tienditajhonyboy.tiendaapp.ui.theme.Slate700
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
    onNavigateToEditProduct: (String) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    var productToDelete by remember { mutableStateOf<com.tienditajhonyboy.tiendaapp.domain.model.Product?>(null) }
    var selectedProductForAction by remember { mutableStateOf<com.tienditajhonyboy.tiendaapp.domain.model.Product?>(null) }
    
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tienda_prefs", Context.MODE_PRIVATE) }
    var storeName by remember { mutableStateOf(prefs.getString("store_name", "Tiendita") ?: "Tiendita") }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    var showImportProductsDialog by remember { mutableStateOf(false) }
    var uriToImportProducts by remember { mutableStateOf<android.net.Uri?>(null) }
    var isImportingProducts by remember { mutableStateOf(false) }

    val importProductsLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            uriToImportProducts = uri
            showImportProductsDialog = true
        }
    }

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

    if (selectedProductForAction != null) {
        AlertDialog(
            onDismissRequest = { selectedProductForAction = null },
            title = { Text("Acciones del Producto") },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            val productId = selectedProductForAction?.id
                            selectedProductForAction = null
                            if (productId != null) {
                                onNavigateToEditProduct(productId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar producto", style = MaterialTheme.typography.bodyLarge)
                    }
                    TextButton(
                        onClick = {
                            productToDelete = selectedProductForAction
                            selectedProductForAction = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar producto", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedProductForAction = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

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

    if (showImportProductsDialog && uriToImportProducts != null) {
        AlertDialog(
            onDismissRequest = { if (!isImportingProducts) showImportProductsDialog = false },
            title = { Text("Importar Productos") },
            text = { 
                Column {
                    Text("¿Qué deseas hacer con el catálogo a importar?")
                    if (isImportingProducts) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                isImportingProducts = true
                                viewModel.importProductsBackup(uriToImportProducts!!, context, replaceData = true, onSuccess = {
                                    isImportingProducts = false
                                    showImportProductsDialog = false
                                    android.widget.Toast.makeText(context, "Catálogo reemplazado", android.widget.Toast.LENGTH_SHORT).show()
                                }, onError = { err ->
                                    isImportingProducts = false
                                    android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                })
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Reemplazar Actuales")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                isImportingProducts = true
                                viewModel.importProductsBackup(uriToImportProducts!!, context, replaceData = false, onSuccess = {
                                    isImportingProducts = false
                                    showImportProductsDialog = false
                                    android.widget.Toast.makeText(context, "Catálogo adjuntado", android.widget.Toast.LENGTH_SHORT).show()
                                }, onError = { err ->
                                    isImportingProducts = false
                                    android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                })
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Añadir")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                if (!isImportingProducts) {
                    TextButton(onClick = { showImportProductsDialog = false }) {
                        Text("Cancelar")
                    }
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            Row {
                IconButton(onClick = { importProductsLauncher.launch("*/*") }) {
                    Icon(Icons.Default.FileUpload, contentDescription = "Importar Productos")
                }
                IconButton(onClick = { 
                    viewModel.exportProductsBackup(context, onSuccess = { file ->
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            type = "application/json"
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Exportar Productos")
                        context.startActivity(shareIntent)
                    }, onError = { err -> 
                        android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                    })
                }) {
                    Icon(Icons.Default.BrowserUpdated, contentDescription = "Exportar Productos")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
        

        
        ProductCarousel(
            products = uiState.productList,
            onProductClick = { product ->
                onNavigateToPOS(product.id)
            },
            onProductLongClick = { product ->
                selectedProductForAction = product
            },
            onAddNewProduct = onNavigateToNewProduct
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionCard(
                icon = { Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                label = "Vender",
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                labelColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPOS(null) }
            )

            ActionCard(
                icon = { Icon(Icons.Default.History, contentDescription = null, tint = Slate700) },
                label = "Historial",
                backgroundColor = Slate100,
                labelColor = Slate700,
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
