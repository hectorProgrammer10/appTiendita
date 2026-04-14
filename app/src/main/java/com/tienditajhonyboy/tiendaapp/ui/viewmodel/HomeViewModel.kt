package com.tienditajhonyboy.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tienditajhonyboy.tiendaapp.domain.model.Product
import com.tienditajhonyboy.tiendaapp.domain.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val productsRepository: ProductRepository) : ViewModel() {
    val homeUiState: StateFlow<HomeUiState> = productsRepository.getAllProducts()
        .map { HomeUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            if (product.image.isNotEmpty()) {
                com.tienditajhonyboy.tiendaapp.util.ImageUtils.deleteImageFile(product.image)
            }
            productsRepository.deleteProduct(product.id)
        }
    }
    fun exportProductsBackup(context: android.content.Context, onSuccess: (java.io.File) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val products = homeUiState.value.productList
                if (products.isEmpty()) {
                    withContext(kotlinx.coroutines.Dispatchers.Main) { onError("No hay productos para exportar.") }
                    return@launch
                }
                
                val dtoList = products.map { p ->
                    ProductBackupDTO(
                        id = p.id,
                        name = p.name,
                        price = p.price,
                        unit = p.unit.name,
                        image_base64 = com.tienditajhonyboy.tiendaapp.util.ImageUtils.encodeImageToBase64(p.image),
                        createdAt = p.createdAt
                    )
                }
                
                val wrapper = ProductBackupWrapper(products = dtoList)
                val jsonString = com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(wrapper)
                
                val fileName = "backup_productos_${System.currentTimeMillis()}.json"
                val file = java.io.File(context.cacheDir, fileName)
                java.io.FileOutputStream(file).use { out ->
                    out.write(jsonString.toByteArray())
                }
                
                withContext(kotlinx.coroutines.Dispatchers.Main) { onSuccess(file) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(kotlinx.coroutines.Dispatchers.Main) { onError("Error al exportar: ${e.message}") }
            }
        }
    }

    fun importProductsBackup(uri: android.net.Uri, context: android.content.Context, replaceData: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("No se pudo leer el archivo")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                
                val gson = com.google.gson.Gson()
                val wrapper = try {
                    gson.fromJson(jsonString, ProductBackupWrapper::class.java)
                } catch (e: Exception) {
                    null
                }
                
                if (wrapper == null || wrapper.products.isEmpty()) {
                    withContext(kotlinx.coroutines.Dispatchers.Main) { onError("Formato incorrecto o archivo vacío.") }
                    return@launch
                }
                
                if (replaceData) {
                    val existingProducts = homeUiState.value.productList
                    existingProducts.forEach { p ->
                        if (p.image.isNotEmpty()) {
                            com.tienditajhonyboy.tiendaapp.util.ImageUtils.deleteImageFile(p.image)
                        }
                        productsRepository.deleteProduct(p.id)
                    }
                }
                
                wrapper.products.forEach { dto ->
                    val newImageStr = com.tienditajhonyboy.tiendaapp.util.ImageUtils.saveBase64ToImage(context, dto.image_base64) ?: ""
                    val unitType = try { com.tienditajhonyboy.tiendaapp.domain.model.UnitType.valueOf(dto.unit) } catch (e: Exception) { com.tienditajhonyboy.tiendaapp.domain.model.UnitType.piece }
                    
                    val product = Product(
                        id = if (replaceData) dto.id else java.util.UUID.randomUUID().toString(),
                        name = dto.name,
                        price = dto.price,
                        unit = unitType,
                        image = newImageStr,
                        createdAt = if (replaceData) dto.createdAt else System.currentTimeMillis()
                    )
                    productsRepository.insertProduct(product)
                }
                
                withContext(kotlinx.coroutines.Dispatchers.Main) { onSuccess() }
                
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(kotlinx.coroutines.Dispatchers.Main) { onError("Error al importar: ${e.message}") }
            }
        }
    }
}

data class HomeUiState(val productList: List<Product> = listOf())

data class ProductBackupWrapper(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val products: List<ProductBackupDTO>
)

data class ProductBackupDTO(
    val id: String,
    val name: String,
    val price: Double,
    val unit: String,
    val image_base64: String?,
    val createdAt: Long
)


