package com.tienditajhonyboy.tiendaapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tienditajhonyboy.tiendaapp.domain.model.CartItem

@Composable
fun SaleItemCard(item: CartItem, onRemove: () -> Unit) {
  Card(
          modifier = Modifier.padding(vertical = 4.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    ListItem(
            headlineContent = { Text(item.productName) },
            supportingContent = { Text("${item.quantity} ${item.unit} x $${item.pricePerUnit}") },
            trailingContent = {
              Text("$${item.subtotal}", style = MaterialTheme.typography.titleMedium)
            },
            leadingContent = {
              IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
              }
            }
    )
  }
}
