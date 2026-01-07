package com.example.tiendaapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tiendaapp.domain.model.UnitType
import com.example.tiendaapp.ui.viewmodel.AppViewModelProvider
import com.example.tiendaapp.ui.viewmodel.ProductNewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductNewScreen(
    viewModel: ProductNewViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(UnitType.kg) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    var showImageOptionsDialog by remember { mutableStateOf(false) }
    var showImageEditorDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Function to create temp file
    fun createTempImageUri(): Uri {
        val cacheDir = context.cacheDir
        if (!cacheDir.exists()) cacheDir.mkdirs()
        
        val tempFile = java.io.File(cacheDir, "product_${System.currentTimeMillis()}.jpg").apply {
            if (exists()) delete()
            createNewFile()
        }
        
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            imageUri = tempImageUri
        }
    }

    if (showImageOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showImageOptionsDialog = false },
            title = { Text("Opciones de Imagen") },
            text = { Text("¿Qué deseas hacer con la imagen?") },
            confirmButton = {
                TextButton(onClick = {
                    showImageOptionsDialog = false
                    showImageSourceDialog = true
                }) {
                    Text("Cambiar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageOptionsDialog = false
                    showImageEditorDialog = true
                }) {
                    Text("Editar")
                }
            }
        )
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Seleccionar Imagen") },
            text = { Text("¿Cómo deseas agregar la imagen?") },
            confirmButton = {
                TextButton(onClick = {
                    tempImageUri = createTempImageUri()
                    cameraLauncher.launch(tempImageUri!!)
                    showImageSourceDialog = false
                }) {
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showImageSourceDialog = false
                }) {
                    Text("Galería")
                }
            }
        )
    }

    if (showImageEditorDialog && imageUri != null) {
        com.example.tiendaapp.ui.components.ImageEditorDialog(
            imageUri = imageUri!!,
            onDismiss = { showImageEditorDialog = false },
            onSave = { newUri ->
                imageUri = newUri
                showImageEditorDialog = false
            }
        )
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { 
                        if (imageUri != null) {
                            showImageOptionsDialog = true
                        } else {
                            showImageSourceDialog = true 
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Product Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate, 
                            contentDescription = null, 
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Agregar Imagen", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Producto") },
                placeholder = { Text("Ej. Filete de Tilapia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Price and Unit Row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Price Input
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio (${if (unit == UnitType.kg) "/ kg" else "/ pza"})") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // Unit Toggle (Segmented Button style simulation)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp) // Match TextField height approximately
                        .clip(RoundedCornerShape(4.dp)) // Material TextField shape
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selectedColor = MaterialTheme.colorScheme.primary
                    val unselectedColor = Color.Transparent
                    val textSelectedColor = MaterialTheme.colorScheme.onTertiary
                    val textUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (unit == UnitType.kg) selectedColor else unselectedColor)
                            .clickable { unit = UnitType.kg },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Kg", color = if(unit == UnitType.kg) textSelectedColor else textUnselectedColor)
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (unit == UnitType.piece) selectedColor else unselectedColor)
                            .clickable { unit = UnitType.piece },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Pieza", color = if(unit == UnitType.piece) textSelectedColor else textUnselectedColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isNotBlank() && price.isNotBlank()) {
                         viewModel.saveProduct(
                            name, 
                            price.toDoubleOrNull() ?: 0.0, 
                            unit, 
                            imageUri?.toString() ?: ""
                         )
                         android.widget.Toast.makeText(context, "Producto creado exitosamente", android.widget.Toast.LENGTH_SHORT).show()
                         onNavigateBack()
                    } else {
                         android.widget.Toast.makeText(context, "Completa todos los campos", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = name.isNotBlank() && price.isNotBlank()
            ) {
                Text("Guardar Producto", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
