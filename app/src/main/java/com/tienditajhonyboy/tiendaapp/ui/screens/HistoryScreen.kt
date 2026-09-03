package com.tienditajhonyboy.tiendaapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrowserUpdated
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tienditajhonyboy.tiendaapp.domain.model.PaymentType
import com.tienditajhonyboy.tiendaapp.domain.model.Sale
import com.tienditajhonyboy.tiendaapp.domain.model.CartItem
import com.tienditajhonyboy.tiendaapp.domain.model.UnitType
import com.tienditajhonyboy.tiendaapp.ui.viewmodel.AppViewModelProvider
import com.tienditajhonyboy.tiendaapp.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.tienditajhonyboy.tiendaapp.ui.theme.SuccessGreen
import com.tienditajhonyboy.tiendaapp.ui.theme.WarningOrange
import com.tienditajhonyboy.tiendaapp.ui.theme.DangerRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateBack: () -> Unit,
    initialImportUri: android.net.Uri? = null
) {
    val uiState by viewModel.historyUiState.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val context = LocalContext.current

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var selectedSaleForEdit by remember { mutableStateOf<Sale?>(null) }
    var showSummaryDialog by remember { mutableStateOf(false) }
    
    var showImportDialog by remember { mutableStateOf(initialImportUri != null) }
    var uriToImport by remember { mutableStateOf<android.net.Uri?>(initialImportUri) }
    var isImporting by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            uriToImport = uri
            showImportDialog = true
        }
    }

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
                    IconButton(onClick = { importLauncher.launch("*/*") }) {
                        Icon(Icons.Default.BrowserUpdated, contentDescription = "Importar")
                    }
                    IconButton(
                        onClick = {
                            isExporting = true
                            viewModel.exportHistoryToExcel(
                                context = context,
                                onSuccess = { file ->
                                    isExporting = false
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
                                },
                                onError = { errorMsg ->
                                    isExporting = false
                                    android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        enabled = !isExporting && uiState.saleList.isNotEmpty()
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.FileUpload, contentDescription = "Exportar")
                        }
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                    items(uiState.saleList, key = { it.id }) { sale ->
                        HistoryItemCard(
                            sale = sale,
                            onClick = { selectedSaleForEdit = sale }
                        )
                    }
                }

            }
            
            if (uiState.saleList.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showSummaryDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    containerColor = SuccessGreen,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ) {
                    Text("$${String.format("%.2f", uiState.summary.totalContado)}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }

        if (showSummaryDialog) {
            val summary = uiState.summary

            AlertDialog(
                onDismissRequest = { showSummaryDialog = false },
                title = { Text("Resumen de Ventas") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Contado (${summary.countContado})", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = SuccessGreen)
                                Text("Total: $${String.format("%.2f", summary.totalContado)}", style = MaterialTheme.typography.titleMedium, color = SuccessGreen)
                            }
                        }
                        Surface(
                            color = WarningOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Pendiente (${summary.countPendiente})", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = WarningOrange)
                                Text("Total: $${String.format("%.2f", summary.totalPendiente)}", style = MaterialTheme.typography.titleMedium, color = WarningOrange)
                            }
                        }
                        Surface(
                            color = DangerRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Cancelado (${summary.countCancelado})", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = DangerRed)
                                Text("Total: $${String.format("%.2f", summary.totalCancelado)}", style = MaterialTheme.typography.titleMedium, color = DangerRed)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSummaryDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }

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
        
        if (showImportDialog && uriToImport != null) {
            AlertDialog(
                onDismissRequest = { if (!isImporting) showImportDialog = false },
                title = { Text("Opciones de Importación") },
                text = { 
                    Column {
                        Text("¿Qué deseas hacer con los datos a importar de este Excel?")
                        if (isImporting) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    isImporting = true
                                    viewModel.importHistoryFromExcel(uriToImport!!, context, replaceData = true, onSuccess = {
                                        isImporting = false
                                        showImportDialog = false
                                        android.widget.Toast.makeText(context, "Datos reemplazados correctamente", android.widget.Toast.LENGTH_SHORT).show()
                                    }, onError = { err ->
                                        isImporting = false
                                        android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                    })
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Reemplazar Actuales (Se borra tu app)")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    isImporting = true
                                    viewModel.importHistoryFromExcel(uriToImport!!, context, replaceData = false, onSuccess = {
                                        isImporting = false
                                        showImportDialog = false
                                        android.widget.Toast.makeText(context, "Datos adjuntados correctamente", android.widget.Toast.LENGTH_SHORT).show()
                                    }, onError = { err ->
                                        isImporting = false
                                        android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                    })
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Añadir (Conserva ambos)")
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    if (!isImporting) {
                        TextButton(onClick = { showImportDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                }
            )
        }

        if (selectedSaleForEdit != null) {
            val sale = selectedSaleForEdit!!
            AlertDialog(
                onDismissRequest = { selectedSaleForEdit = null },
                title = { Text("Editar Venta") },
                text = {
                    Column {
                        Text("Cliente: ${sale.clientName ?: "Anonimo"}", style = MaterialTheme.typography.titleMedium)
                        if (sale.paymentType == PaymentType.contado) {
                            Text("Recibido: $${String.format("%.2f", sale.paymentAmount)}", style = MaterialTheme.typography.titleMedium)
                            Text("Cambio: $${String.format("%.2f", sale.change)}", style = MaterialTheme.typography.titleMedium)
                        }
                        Text("Total: $${String.format("%.2f", sale.total)}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Productos:", style = MaterialTheme.typography.titleSmall)
                        sale.items.forEach { item ->
                            val qtyText = if (item.unit == UnitType.kg) 
                                "${String.format("%.2f", item.quantity)} kg" 
                            else 
                                "${item.quantity.toInt()} pz"
                            Text(
                                "• ${item.productName}: $qtyText - $${String.format("%.2f", item.subtotal)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cambiar Estado a:")
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { 
                                viewModel.updateSaleStatus(sale.id, PaymentType.contado) 
                                selectedSaleForEdit = null
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = sale.paymentType != PaymentType.contado
                        ) {
                            Text("Contado", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { 
                                viewModel.updateSaleStatus(sale.id, PaymentType.pendiente)
                                selectedSaleForEdit = null
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = sale.paymentType != PaymentType.pendiente
                        ) {
                            Text("Pendiente", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))                        
                        Button(
                            onClick = { 
                                viewModel.updateSaleStatus(sale.id, PaymentType.cancelado)
                                selectedSaleForEdit = null
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            enabled = sale.paymentType != PaymentType.cancelado
                        ) {
                            Text("Cancelado", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { selectedSaleForEdit = null }) {
                        Text("Cerrar", style = MaterialTheme.typography.titleMedium)
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(top = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val dateFormat = remember { SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()) }
                Text(
                    text = dateFormat.format(Date(sale.date)),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = sale.clientName?.takeIf { it.isNotBlank() } ?: "----",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    text = buildProductSummary(sale.items), 
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", sale.total)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                val statusColor = when(sale.paymentType) {
                    PaymentType.contado -> SuccessGreen
                    PaymentType.pendiente -> WarningOrange
                    PaymentType.cancelado -> DangerRed
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = sale.paymentType.name.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor
                    )
                }
            }
        }
    }
}

private fun buildProductSummary(items: List<CartItem>): String {
    val pieces = items.filter { it.unit == UnitType.piece }.sumOf { it.quantity.toInt() }
    val kgTotal = items.filter { it.unit == UnitType.kg }.sumOf { it.quantity }
    
    return buildString {
        if (pieces > 0) append("$pieces pzs")
        if (pieces > 0 && kgTotal > 0) append(" + ")
        if (kgTotal > 0) append("${String.format("%.2f", kgTotal)} kg")
        if (pieces == 0 && kgTotal == 0.0) append("Sin productos")
    }
}
