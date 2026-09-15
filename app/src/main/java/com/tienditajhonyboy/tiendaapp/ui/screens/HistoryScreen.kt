package com.tienditajhonyboy.tiendaapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrowserUpdated
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tienditajhonyboy.tiendaapp.domain.model.CartItem
import com.tienditajhonyboy.tiendaapp.domain.model.PaymentType
import com.tienditajhonyboy.tiendaapp.domain.model.Sale
import com.tienditajhonyboy.tiendaapp.domain.model.UnitType
import com.tienditajhonyboy.tiendaapp.ui.theme.DangerRed
import com.tienditajhonyboy.tiendaapp.ui.theme.SuccessGreen
import com.tienditajhonyboy.tiendaapp.ui.theme.WarningOrange
import com.tienditajhonyboy.tiendaapp.ui.viewmodel.AppViewModelProvider
import com.tienditajhonyboy.tiendaapp.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val totalSalesCount = uiState.summary.countContado + uiState.summary.countPendiente + uiState.summary.countCancelado

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedDeleteOption by remember { mutableStateOf<PaymentType?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var showExportDialog by remember { mutableStateOf(false) }
    var selectedExportOption by remember { mutableStateOf<PaymentType?>(null) }

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
                            selectedExportOption = null
                            showExportDialog = true
                        },
                        enabled = !isExporting && totalSalesCount > 0
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
            if (totalSalesCount > 0) {
                FloatingActionButton(
                    onClick = {
                        selectedDeleteOption = null
                        showDeleteDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Historial")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
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
                    FilterChip(
                        selected = currentFilter == PaymentType.cancelado,
                        onClick = { viewModel.setFilter(PaymentType.cancelado) },
                        label = { Text("Cancelado") }
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

        if (showDeleteDialog) {
            val summary = uiState.summary
            val countForSelected = when (selectedDeleteOption) {
                null -> summary.countContado + summary.countPendiente + summary.countCancelado
                PaymentType.contado -> summary.countContado
                PaymentType.pendiente -> summary.countPendiente
                PaymentType.cancelado -> summary.countCancelado
            }

            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                title = { Text("Eliminar Historial", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Selecciona qué registros deseas eliminar:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val totalCount = summary.countContado + summary.countPendiente + summary.countCancelado
                        HistorySelectionOptionRow(
                            title = "Todo el historial",
                            subtitle = "$totalCount ventas en total",
                            selected = selectedDeleteOption == null,
                            onClick = { selectedDeleteOption = null }
                        )
                        
                        HistorySelectionOptionRow(
                            title = "Solo Contado",
                            subtitle = "${summary.countContado} ventas ($${String.format("%.2f", summary.totalContado)})",
                            selected = selectedDeleteOption == PaymentType.contado,
                            accentColor = SuccessGreen,
                            onClick = { selectedDeleteOption = PaymentType.contado }
                        )
                        
                        HistorySelectionOptionRow(
                            title = "Solo Pendiente",
                            subtitle = "${summary.countPendiente} ventas ($${String.format("%.2f", summary.totalPendiente)})",
                            selected = selectedDeleteOption == PaymentType.pendiente,
                            accentColor = WarningOrange,
                            onClick = { selectedDeleteOption = PaymentType.pendiente }
                        )
                        
                        HistorySelectionOptionRow(
                            title = "Solo Cancelado",
                            subtitle = "${summary.countCancelado} ventas ($${String.format("%.2f", summary.totalCancelado)})",
                            selected = selectedDeleteOption == PaymentType.cancelado,
                            accentColor = DangerRed,
                            onClick = { selectedDeleteOption = PaymentType.cancelado }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            showDeleteConfirmDialog = true
                        },
                        enabled = countForSelected > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Continuar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showDeleteConfirmDialog) {
            val optionName = when (selectedDeleteOption) {
                null -> "TODO el historial"
                PaymentType.contado -> "las ventas de CONTADO"
                PaymentType.pendiente -> "las ventas PENDIENTES"
                PaymentType.cancelado -> "las ventas CANCELADAS"
            }

            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                icon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                title = { Text("¿Confirmar Eliminación?") },
                text = {
                    Text(
                        "Estás a punto de eliminar permanentemente $optionName. Esta acción NO se puede deshacer.\n\n¿Deseas proceder?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val filterToDelete = selectedDeleteOption
                            showDeleteConfirmDialog = false
                            viewModel.deleteSales(filterToDelete) {
                                android.widget.Toast.makeText(context, "Ventas eliminadas correctamente", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Sí, Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showExportDialog) {
            val summary = uiState.summary
            val countForSelected = when (selectedExportOption) {
                null -> summary.countContado + summary.countPendiente + summary.countCancelado
                PaymentType.contado -> summary.countContado
                PaymentType.pendiente -> summary.countPendiente
                PaymentType.cancelado -> summary.countCancelado
            }

            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                icon = {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                title = { Text("Exportar a Excel", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Selecciona qué ventas deseas exportar:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val totalCount = summary.countContado + summary.countPendiente + summary.countCancelado
                        HistorySelectionOptionRow(
                            title = "Todo el historial",
                            subtitle = "$totalCount ventas en total",
                            selected = selectedExportOption == null,
                            onClick = { selectedExportOption = null }
                        )
                        
                        HistorySelectionOptionRow(
                            title = "Solo Contado",
                            subtitle = "${summary.countContado} ventas ($${String.format("%.2f", summary.totalContado)})",
                            selected = selectedExportOption == PaymentType.contado,
                            accentColor = SuccessGreen,
                            onClick = { selectedExportOption = PaymentType.contado }
                        )
                        
                        HistorySelectionOptionRow(
                            title = "Solo Pendiente",
                            subtitle = "${summary.countPendiente} ventas ($${String.format("%.2f", summary.totalPendiente)})",
                            selected = selectedExportOption == PaymentType.pendiente,
                            accentColor = WarningOrange,
                            onClick = { selectedExportOption = PaymentType.pendiente }
                        )
                        
                        HistorySelectionOptionRow(
                            title = "Solo Cancelado",
                            subtitle = "${summary.countCancelado} ventas ($${String.format("%.2f", summary.totalCancelado)})",
                            selected = selectedExportOption == PaymentType.cancelado,
                            accentColor = DangerRed,
                            onClick = { selectedExportOption = PaymentType.cancelado }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val filterToExport = selectedExportOption
                            showExportDialog = false
                            isExporting = true
                            viewModel.exportHistoryToExcel(
                                context = context,
                                filterType = filterToExport,
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
                        enabled = countForSelected > 0
                    ) {
                        Text("Exportar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
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
private fun HistorySelectionOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    accentColor: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        },
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accentColor ?: MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                    color = accentColor ?: MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
