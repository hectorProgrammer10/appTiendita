package com.example.tiendaapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tiendaapp.domain.model.PaymentType

@Composable
fun PaymentDialog(
    total: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, PaymentType, String?) -> Unit
) {
    var amountReceived by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf(PaymentType.contado) }
    var clientName by remember { mutableStateOf("") }

    val amount = amountReceived.toDoubleOrNull() ?: 0.0
    val change = if (amount > total) amount - total else 0.0
    val isValid = if (paymentType == PaymentType.contado) amount >= total else clientName.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                   modifier = Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.SpaceBetween,
                   verticalAlignment = Alignment.CenterVertically 
                ) {
                    Text(
                        text = "Finalizar Venta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TOTAL A PAGAR", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        text = "$${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1) // Blue
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Toggle (Segmented Control simulation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    val selectedColor = Color(0xFF0288D1)
                    val unselectedColor = Color.White
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (paymentType == PaymentType.contado) selectedColor else unselectedColor)
                            .clickable { paymentType = PaymentType.contado },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Contado", 
                            color = if (paymentType == PaymentType.contado) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color.LightGray)
                    ) {}
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (paymentType == PaymentType.pendiente) selectedColor else unselectedColor)
                            .clickable { paymentType = PaymentType.pendiente },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Pendiente", 
                            color = if (paymentType == PaymentType.pendiente) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (paymentType == PaymentType.contado) {
                    // Removed extra border applied directly to TextField parent Row? No, it was just Floating.
                    // The previous code had a stray border modifier line. Removed it.
                    OutlinedTextField(
                        value = amountReceived,
                        onValueChange = { amountReceived = it },
                        label = { Text("MONTO RECIBIDO") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cambio:", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "$${String.format("%.2f", change)}", 
                            color = Color(0xFF00C853), // Green
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Nombre del Cliente") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val finalAmount = if (paymentType == PaymentType.pendiente) 0.0 else amount
                        onConfirm(finalAmount, paymentType, clientName.takeIf { it.isNotBlank() })
                    },
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                         containerColor = Color(0xFF81D4FA), // Light Blue
                         contentColor = Color(0xFF01579B), // Dark Blue Text
                         disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text("Confirmar Venta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}


