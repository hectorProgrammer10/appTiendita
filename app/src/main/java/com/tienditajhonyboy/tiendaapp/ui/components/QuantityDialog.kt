package com.tienditajhonyboy.tiendaapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tienditajhonyboy.tiendaapp.domain.model.Product
import com.tienditajhonyboy.tiendaapp.ui.theme.SuccessGreen
import com.tienditajhonyboy.tiendaapp.ui.theme.InfoBlue

@Composable
fun QuantityDialog(product: Product, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var quantity by remember { mutableStateOf("") }
    var totalText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AGREGANDO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Peso (${if(product.unit == com.tienditajhonyboy.tiendaapp.domain.model.UnitType.kg) "Kilogramos" else "Piezas"})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { 
                        quantity = it 
                        val pQty = it.toDoubleOrNull() ?: 0.0
                        totalText = if (pQty > 0) String.format(java.util.Locale.US, "%.2f", pQty * product.price) else ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("0", textAlign = TextAlign.Center) },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                    trailingIcon = { Text(if(product.unit == com.tienditajhonyboy.tiendaapp.domain.model.UnitType.kg) "kg" else "pz") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                val parsedQty = quantity.toDoubleOrNull() ?: 0.0
                
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { 
                        totalText = it 
                        val pTotal = it.toDoubleOrNull() ?: 0.0
                        quantity = if (pTotal > 0 && product.price > 0) {
                            val calculatedQty = pTotal / product.price
                            val formatted = String.format(java.util.Locale.US, "%.3f", calculatedQty)
                            formatted.trimEnd('0').trimEnd('.')
                        } else ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Total aprox.") },
                    leadingIcon = { Text("$", color = SuccessGreen, style = MaterialTheme.typography.headlineSmall) },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(color = SuccessGreen, textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    readOnly = product.unit != com.tienditajhonyboy.tiendaapp.domain.model.UnitType.kg
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onConfirm(parsedQty) },
                    enabled = parsedQty > 0,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                         containerColor = InfoBlue
                    )
                ) {
                    Text("Agregar al Carrito", fontSize = 16.sp)
                }
            }
        }
    }
}
