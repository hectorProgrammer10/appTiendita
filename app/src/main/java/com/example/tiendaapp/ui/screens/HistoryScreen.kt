package com.example.tiendaapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendaapp.domain.model.PaymentType
import com.example.tiendaapp.domain.model.Sale
import com.example.tiendaapp.ui.viewmodel.AppViewModelProvider
import com.example.tiendaapp.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.historyUiState.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val context = LocalContext.current

    // Dialog States
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var selectedSaleForEdit by remember { mutableStateOf<Sale?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Ventas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val file = viewModel.exportHistoryToExcel(context)
                        if (file != null) {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, uri)
                                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Exportar Ventas Excel")
                            context.startActivity(shareIntent)
                        } else {
                            android.widget.Toast.makeText(context, "Error al exportar o no hay datos", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.saleList.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showDeleteAllDialog = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar Todo")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentFilter == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = currentFilter == PaymentType.contado,
                    onClick = { viewModel.setFilter(PaymentType.contado) },
                    label = { Text("Contado") }
                )
                FilterChip(
                    selected = currentFilter == PaymentType.pendiente,
                    onClick = { viewModel.setFilter(PaymentType.pendiente) },
                    label = { Text("Pendiente") }
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.saleList) { sale ->
                    HistoryItemCard(
                        sale = sale,
                        onClick = { selectedSaleForEdit = sale }
                    )
                }
            }
        }

        // Delete All Confirmation
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("¿Eliminar Historial?") },
                text = { Text("Estás a punto de borrar todas las ventas. Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearHistory()
                            showDeleteAllDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar Todo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Edit Status Dialog
        if (selectedSaleForEdit != null) {
            val sale = selectedSaleForEdit!!
            AlertDialog(
                onDismissRequest = { selectedSaleForEdit = null },
                title = { Text("Editar Venta") },
                text = {
                    Column {
                        Text("Cliente: ${sale.clientName ?: "Anonimo"}")
                        Text("Total: $${sale.total}")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cambiar Estado a:")
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { 
                                viewModel.updateSaleStatus(sale.id, PaymentType.contado) 
                                selectedSaleForEdit = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = sale.paymentType != PaymentType.contado
                        ) {
                            Text("Contado")
                        }
                        
                        Button(
                            onClick = { 
                                viewModel.updateSaleStatus(sale.id, PaymentType.pendiente)
                                selectedSaleForEdit = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = sale.paymentType != PaymentType.pendiente
                        ) {
                            Text("Pendiente")
                        }
                        
                        Button(
                            onClick = { 
                                viewModel.updateSaleStatus(sale.id, PaymentType.cancelado)
                                selectedSaleForEdit = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            enabled = sale.paymentType != PaymentType.cancelado
                        ) {
                            Text("Cancelado")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { selectedSaleForEdit = null }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

@Composable
fun HistoryItemCard(sale: Sale, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Date and Client
            Column(modifier = Modifier.weight(1f)) {
                val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
                Text(
                    text = dateFormat.format(Date(sale.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = sale.clientName?.takeIf { it.isNotBlank() } ?: "----",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    text = "${sale.items.sumOf { it.quantity.toInt() }} productos", 
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Right: Total and Status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", sale.total)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                val statusColor = when(sale.paymentType) {
                    PaymentType.contado -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
                    PaymentType.pendiente -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
                    PaymentType.cancelado -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = sale.paymentType.name.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
        }
    }
}
