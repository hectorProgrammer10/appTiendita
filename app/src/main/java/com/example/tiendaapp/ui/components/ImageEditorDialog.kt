package com.example.tiendaapp.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImageEditorDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onSave: (Uri) -> Unit
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Load bitmap
    LaunchedEffect(imageUri) {
        context.contentResolver.openInputStream(imageUri)?.use { stream ->
            bitmap = BitmapFactory.decodeStream(stream)
                ?.copy(Bitmap.Config.ARGB_8888, true) // Make mutable
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                         Image(
                             bitmap = bitmap!!.asImageBitmap(),
                             contentDescription = "Editing Image",
                             modifier = Modifier.fillMaxSize(),
                             contentScale = androidx.compose.ui.layout.ContentScale.Fit
                         )
                         
                         // Visual text overlay preview (Simplified, actual drawing happens on save)
                         if (text.isNotEmpty()) {
                             Text(
                                 text = text,
                                 color = androidx.compose.ui.graphics.Color.Yellow, // Simplified preview
                                 style = MaterialTheme.typography.displayLarge,
                                 modifier = Modifier.align(Alignment.Center)
                             )
                         }
                    } else {
                        CircularProgressIndicator()
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Texto para la imagen") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Button(onClick = {
                        if (bitmap != null) {
                            val resultUri = saveImageWithText(context, bitmap!!, text)
                            if (resultUri != null) {
                                onSave(resultUri)
                            } else {
                                android.widget.Toast.makeText(context, "Error saving", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

private fun saveImageWithText(context: Context, originalBitmap: Bitmap, text: String): Uri? {
    return try {
        // Draw text on bitmap
        val workingBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)
        
        if (text.isNotEmpty()) {
            val paint = Paint().apply {
                color = Color.YELLOW
                textSize = workingBitmap.height * 0.16f // 10% of height
                textAlign = Paint.Align.CENTER
                setShadowLayer(10f, 0f, 0f, Color.BLACK)
            }
            val x = canvas.width / 2f
            val y = canvas.height / 2f
            canvas.drawText(text, x, y, paint)
        }

        // Save to file
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "edited_${System.currentTimeMillis()}.jpg")
        val stream = FileOutputStream(file)
        workingBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        stream.close()

        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
