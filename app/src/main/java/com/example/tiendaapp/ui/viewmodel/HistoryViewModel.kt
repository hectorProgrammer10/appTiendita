package com.example.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendaapp.domain.model.PaymentType
import com.example.tiendaapp.domain.model.Sale
import com.example.tiendaapp.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
            
            // Header
            val headerRow = sheet.createRow(0)
            val headers = listOf("ID", "Fecha", "Cliente", "Productos", "Total", "Tipo")
            headers.forEachIndexed { index, title ->
                headerRow.createCell(index).setCellValue(title)
            }

            // Data
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            sales.forEachIndexed { index, sale ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(sale.id.take(8))
                row.createCell(1).setCellValue(dateFormat.format(java.util.Date(sale.date)))
                row.createCell(2).setCellValue(sale.clientName ?: "-")
                
                val items = sale.items.joinToString("; ") { "${it.quantity} ${it.unit} ${it.productName}" }
                row.createCell(3).setCellValue(items)
                
                row.createCell(4).setCellValue(sale.total)
                row.createCell(5).setCellValue(sale.paymentType.toString().uppercase())
            }

            val fileName = "ventas_pescaderia_${System.currentTimeMillis()}.xlsx"
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
}

data class HistoryUiState(val saleList: List<Sale> = listOf())
