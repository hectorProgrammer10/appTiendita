package com.tienditajhonyboy.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tienditajhonyboy.tiendaapp.domain.model.PaymentType
import com.tienditajhonyboy.tiendaapp.domain.model.Sale
import com.tienditajhonyboy.tiendaapp.domain.repository.SaleRepository
import com.tienditajhonyboy.tiendaapp.domain.repository.WorkspaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryViewModel(
    private val saleRepository: SaleRepository,
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    private val _filter = MutableStateFlow<PaymentType?>(null) // null = All
    val filter: StateFlow<PaymentType?> = _filter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val activeWorkspaceSales: Flow<List<Sale>> = workspaceRepository.getActiveWorkspaceId()
        .flatMapLatest { workspaceId ->
            saleRepository.getSalesByWorkspace(workspaceId)
        }

    val historyUiState: StateFlow<HistoryUiState> = 
        combine(activeWorkspaceSales, _filter) { sales, filter ->
            var totalContado = 0.0
            var countContado = 0
            var totalPendiente = 0.0
            var countPendiente = 0
            var totalCancelado = 0.0
            var countCancelado = 0

            val filtered = mutableListOf<Sale>()
            for (sale in sales) {
                when (sale.paymentType) {
                    PaymentType.contado -> {
                        totalContado += sale.total
                        countContado++
                    }
                    PaymentType.pendiente -> {
                        totalPendiente += sale.total
                        countPendiente++
                    }
                    PaymentType.cancelado -> {
                        totalCancelado += sale.total
                        countCancelado++
                    }
                }
                if (filter == null || sale.paymentType == filter) {
                    filtered.add(sale)
                }
            }

            val summary = SalesSummary(
                totalContado = totalContado,
                countContado = countContado,
                totalPendiente = totalPendiente,
                countPendiente = countPendiente,
                totalCancelado = totalCancelado,
                countCancelado = countCancelado
            )

            HistoryUiState(saleList = filtered, summary = summary)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState()
        )

    fun setFilter(type: PaymentType?) {
        _filter.value = type
    }
    
    fun updateSaleStatus(saleId: String, newStatus: PaymentType) {
        viewModelScope.launch {
            saleRepository.updateSaleStatus(saleId, newStatus)
        }
    }

    fun deleteSale(saleId: String) {
        viewModelScope.launch {
            saleRepository.deleteSale(saleId)
        }
    }

    fun deleteSales(type: PaymentType?, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val activeWorkspaceId = workspaceRepository.getActiveWorkspaceId().first()
            if (type == null) {
                saleRepository.deleteSalesByWorkspace(activeWorkspaceId)
            } else {
                saleRepository.deleteSalesByWorkspaceAndType(activeWorkspaceId, type)
            }
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                onComplete?.invoke()
            }
        }
    }

    fun clearHistory() {
        deleteSales(null)
    }

    fun exportHistoryToExcel(
        context: android.content.Context,
        filterType: PaymentType? = null,
        onSuccess: (java.io.File) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val activeWorkspaceId = workspaceRepository.getActiveWorkspaceId().first()
                val allSales = saleRepository.getSalesByWorkspace(activeWorkspaceId).first()
                val sales = if (filterType == null) allSales else allSales.filter { it.paymentType == filterType }
                
                if (sales.isEmpty()) {
                    val filterLabel = when (filterType) {
                        PaymentType.contado -> "de contado"
                        PaymentType.pendiente -> "pendientes"
                        PaymentType.cancelado -> "canceladas"
                        null -> "registradas"
                    }
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onError("No hay ventas $filterLabel para exportar.")
                    }
                    return@launch
                }

                val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
                val sheetName = when (filterType) {
                    PaymentType.contado -> "Ventas Contado"
                    PaymentType.pendiente -> "Ventas Pendiente"
                    PaymentType.cancelado -> "Ventas Canceladas"
                    null -> "Todas las Ventas"
                }
                val sheet = workbook.createSheet(sheetName)
                
                val headerRow = sheet.createRow(0)
                val headers = listOf("ID", "Fecha_Texto", "Fecha_Timestamp", "Cliente", "Total", "Monto_Recibido", "Cambio", "Estado", "Resumen_Productos", "Datos_Raw_Productos")
                headers.forEachIndexed { index, title ->
                    headerRow.createCell(index).setCellValue(title)
                }

                val gson = com.google.gson.Gson()
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                sales.forEachIndexed { index, sale ->
                    val row = sheet.createRow(index + 1)
                    row.createCell(0).setCellValue(sale.id)
                    row.createCell(1).setCellValue(dateFormat.format(java.util.Date(sale.date)))
                    row.createCell(2).setCellValue(sale.date.toString()) // Written as string to avoid Double numeric issues
                    row.createCell(3).setCellValue(sale.clientName ?: "-")
                    
                    row.createCell(4).setCellValue(sale.total)
                    row.createCell(5).setCellValue(sale.paymentAmount)
                    row.createCell(6).setCellValue(sale.change)
                    row.createCell(7).setCellValue(sale.paymentType.name.uppercase())
                    
                    val items = sale.items.joinToString("; ") { "${it.quantity} ${it.unit} ${it.productName}" }
                    row.createCell(8).setCellValue(items)
                    
                    val rawData = gson.toJson(sale.items)
                    row.createCell(9).setCellValue(rawData)
                }

                val suffix = filterType?.name ?: "todas"
                val fileName = "historial_ventas_${suffix}_${System.currentTimeMillis()}.xlsx"
                val file = java.io.File(context.cacheDir, fileName)
                java.io.FileOutputStream(file).use { fileOut ->
                    workbook.write(fileOut)
                }
                workbook.close()

                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onSuccess(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError("Error al exportar: ${e.message}")
                }
            }
        }
    }

    fun importHistoryFromExcel(
        uri: android.net.Uri, 
        context: android.content.Context, 
        replaceData: Boolean, 
        onSuccess: () -> Unit, 
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val activeWorkspaceId = workspaceRepository.getActiveWorkspaceId().first()
                val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("No se pudo leer el archivo")
                val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)
                
                val expectedHeaders = listOf("ID", "Fecha_Texto", "Fecha_Timestamp", "Cliente", "Total", "Monto_Recibido", "Cambio", "Estado", "Resumen_Productos", "Datos_Raw_Productos")
                val headerRow = sheet.getRow(0)
                
                if (headerRow == null || headerRow.physicalNumberOfCells < expectedHeaders.size) {
                    withContext(kotlinx.coroutines.Dispatchers.Main) { onError("Formato incorrecto. No cuenta con las ${expectedHeaders.size} columnas.") }
                    workbook.close()
                    inputStream.close()
                    return@launch
                }
                
                for (i in expectedHeaders.indices) {
                    val cellValue = headerRow.getCell(i)?.stringCellValue ?: ""
                    if (cellValue != expectedHeaders[i]) {
                        withContext(kotlinx.coroutines.Dispatchers.Main) { onError("Formato incorrecto. Columna esperada: ${expectedHeaders[i]}") }
                        workbook.close()
                        inputStream.close()
                        return@launch
                    }
                }
                
                val newSales = mutableListOf<Sale>()
                val gson = com.google.gson.Gson()
                val listType = object : com.google.gson.reflect.TypeToken<List<com.tienditajhonyboy.tiendaapp.domain.model.CartItem>>() {}.type
                
                val existingBatchIds = mutableSetOf<String>()
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    
                    val rawIdCell = row.getCell(0)
                    val rawId = if (rawIdCell?.cellType == org.apache.poi.ss.usermodel.CellType.STRING) rawIdCell.stringCellValue else java.util.UUID.randomUUID().toString()
                    var saleId = if (rawId.length == 8 || rawId.isBlank()) java.util.UUID.randomUUID().toString() else rawId

                    if (!replaceData) {
                        while (saleRepository.getSaleById(saleId) != null || existingBatchIds.contains(saleId)) {
                            saleId = java.util.UUID.randomUUID().toString()
                        }
                    } else {
                        while (existingBatchIds.contains(saleId)) {
                            saleId = java.util.UUID.randomUUID().toString()
                        }
                    }
                    existingBatchIds.add(saleId)
                    
                    val dateCell = row.getCell(2)
                    val dateTimestampStr = if (dateCell?.cellType == org.apache.poi.ss.usermodel.CellType.STRING) dateCell.stringCellValue else ""
                    val date = dateTimestampStr.toLongOrNull() ?: System.currentTimeMillis()
                    
                    val clientCell = row.getCell(3)
                    val clientRaw = if (clientCell?.cellType == org.apache.poi.ss.usermodel.CellType.STRING) clientCell.stringCellValue else "-"
                    val clientName = if (clientRaw == "-" || clientRaw.isNullOrBlank()) null else clientRaw
                    
                    val total = row.getCell(4)?.numericCellValue ?: 0.0
                    val montoRecibido = row.getCell(5)?.numericCellValue ?: total
                    val cambio = row.getCell(6)?.numericCellValue ?: 0.0
                    
                    val estadoCell = row.getCell(7)
                    val estadoStr = if (estadoCell?.cellType == org.apache.poi.ss.usermodel.CellType.STRING) estadoCell.stringCellValue else "CONTADO"
                    val paymentType = try { PaymentType.valueOf(estadoStr.lowercase()) } catch (e: Exception) { PaymentType.contado }
                    
                    val rawDataCell = row.getCell(9)
                    val rawDataStr = if (rawDataCell?.cellType == org.apache.poi.ss.usermodel.CellType.STRING) rawDataCell.stringCellValue else "[]"
                    val items: List<com.tienditajhonyboy.tiendaapp.domain.model.CartItem> = try { gson.fromJson(rawDataStr, listType) } catch (e: Exception) { emptyList() }
                    
                    newSales.add(
                        Sale(
                            id = saleId,
                            workspaceId = activeWorkspaceId,
                            items = items,
                            total = total,
                            paymentAmount = montoRecibido,
                            change = cambio,
                            paymentType = paymentType,
                            clientName = clientName,
                            date = date
                        )
                    )
                }
                
                workbook.close()
                inputStream.close()
                
                if (replaceData) {
                    saleRepository.deleteSalesByWorkspace(activeWorkspaceId)
                }
                
                newSales.forEach { saleRepository.insertSale(it) }
                
                withContext(kotlinx.coroutines.Dispatchers.Main) { onSuccess() }
                
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(kotlinx.coroutines.Dispatchers.Main) { onError("Error al procesar archivo: ${e.message}") }
            }
        }
    }
}

data class SalesSummary(
    val totalContado: Double = 0.0,
    val countContado: Int = 0,
    val totalPendiente: Double = 0.0,
    val countPendiente: Int = 0,
    val totalCancelado: Double = 0.0,
    val countCancelado: Int = 0
)

data class HistoryUiState(
    val saleList: List<Sale> = listOf(),
    val summary: SalesSummary = SalesSummary()
)
