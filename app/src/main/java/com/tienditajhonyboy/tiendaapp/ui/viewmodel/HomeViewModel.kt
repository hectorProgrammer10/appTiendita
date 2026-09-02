package com.tienditajhonyboy.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tienditajhonyboy.tiendaapp.domain.model.Product
import com.tienditajhonyboy.tiendaapp.domain.model.Workspace
import com.tienditajhonyboy.tiendaapp.domain.repository.ProductRepository
import com.tienditajhonyboy.tiendaapp.domain.repository.WorkspaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class HomeViewModel(
    private val productsRepository: ProductRepository,
    private val workspaceRepository: WorkspaceRepository
) : ViewModel() {

    val workspacesState: StateFlow<List<Workspace>> = workspaceRepository.getAllWorkspaces()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = listOf()
        )

    val activeWorkspaceIdState: StateFlow<String> = workspaceRepository.getActiveWorkspaceId()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "ws_default"
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val homeUiState: StateFlow<HomeUiState> = workspaceRepository.getActiveWorkspaceId()
        .flatMapLatest { workspaceId ->
            productsRepository.getProductsByWorkspace(workspaceId)
        }
        .map { HomeUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun selectWorkspace(id: String) {
        viewModelScope.launch {
            workspaceRepository.setActiveWorkspaceId(id)
        }
    }

    fun createWorkspace(name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (name.isBlank()) {
            onError("El nombre no puede estar vacío.")
            return
        }
        viewModelScope.launch {
            val currentWorkspaces = workspaceRepository.getAllWorkspaces().first()
            if (currentWorkspaces.size >= 3) {
                onError("Límite alcanzado: Máximo 3 espacios de trabajo.")
                return@launch
            }
            val newId = "ws_${UUID.randomUUID()}"
            val newWorkspace = Workspace(
                id = newId,
                name = name.trim(),
                createdAt = System.currentTimeMillis()
            )
            workspaceRepository.insertWorkspace(newWorkspace)
            workspaceRepository.setActiveWorkspaceId(newId)
            onSuccess()
        }
    }

    fun updateActiveWorkspaceName(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val activeId = workspaceRepository.getActiveWorkspaceId().first()
            workspaceRepository.updateWorkspaceName(activeId, newName.trim())
        }
    }

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
                    withContext(kotlinx.coroutines.Dispatchers.Main) { onError("No hay productos para exportar en este espacio.") }
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
                val activeWorkspaceId = workspaceRepository.getActiveWorkspaceId().first()
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
                    }
                    productsRepository.deleteProductsByWorkspace(activeWorkspaceId)
                }
                
                wrapper.products.forEach { dto ->
                    val newImageStr = com.tienditajhonyboy.tiendaapp.util.ImageUtils.saveBase64ToImage(context, dto.image_base64) ?: ""
                    val unitType = try { com.tienditajhonyboy.tiendaapp.domain.model.UnitType.valueOf(dto.unit) } catch (e: Exception) { com.tienditajhonyboy.tiendaapp.domain.model.UnitType.piece }
                    
                    var productId = if (replaceData && dto.id.isNotBlank()) dto.id else java.util.UUID.randomUUID().toString()
                    if (!replaceData || productId.isBlank()) {
                        do {
                            productId = java.util.UUID.randomUUID().toString()
                        } while (productsRepository.getProductById(productId) != null)
                    }

                    val product = Product(
                        id = productId,
                        workspaceId = activeWorkspaceId,
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
