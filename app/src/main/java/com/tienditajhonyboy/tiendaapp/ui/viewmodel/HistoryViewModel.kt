package com.tienditajhonyboy.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tienditajhonyboy.tiendaapp.domain.model.PaymentType
import com.tienditajhonyboy.tiendaapp.domain.model.Sale
import com.tienditajhonyboy.tiendaapp.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryViewModel(private val saleRepository: SaleRepository) : ViewModel() {

    private val _filter = MutableStateFlow<PaymentType?>(null) // null = All
    val filter: StateFlow<PaymentType?> = _filter.asStateFlow()

    val historyUiState: StateFlow<HistoryUiState> = 
        combine(saleRepository.getAllSales(), _filter) { sales, filter ->
            val filtered = if (filter == null) sales else sales.filter { it.paymentType == filter }
            HistoryUiState(filtered)
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

    fun clearHistory() {
        viewModelScope.launch {
            saleRepository.deleteAllSales()
        }
    }

    fun exportHistoryToExcel(context: android.content.Context): java.io.File? {
        val sales = historyUiState.value.saleList
        if (sales.isEmpty()) return null

        try {
            val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
            val sheet = workbook.createSheet("Ventas")
            
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

            val fileName = "historial_ventas_${System.currentTimeMillis()}.xlsx"
            val file = java.io.File(context.cacheDir, fileName)
            val fileOut = java.io.FileOutputStream(file)
            workbook.write(fileOut)
            fileOut.close()
            workbook.close()
            
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
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
                
                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    
                    val rawIdCell = row.getCell(0)
                    val rawId = if (rawIdCell?.cellType == org.apache.poi.ss.usermodel.CellType.STRING) rawIdCell.stringCellValue else java.util.UUID.randomUUID().toString()
                    val id = if (rawId.length == 8) java.util.UUID.randomUUID().toString() else rawId
                    
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
                            id = id,
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
                    saleRepository.deleteAllSales()
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

data class HistoryUiState(val saleList: List<Sale> = listOf())
