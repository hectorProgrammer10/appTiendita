package com.example.tiendaapp.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Add
import coil.compose.AsyncImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tiendaapp.domain.model.Product

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductCarousel(
        products: List<Product>,
        onProductClick: (Product) -> Unit,
        onProductLongClick: (Product) -> Unit,
        onAddNewProduct: () -> Unit,
        modifier: Modifier = Modifier
) {
  LazyRow(
          modifier = modifier,
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          contentPadding = PaddingValues(horizontal = 16.dp)
  ) {
    items(products, key = { it.id }) { product ->
      ProductCard(
          product = product, 
          onClick = { onProductClick(product) },
          onLongClick = { onProductLongClick(product) }
      )
    }
    item {
        NewProductCard(onClick = onAddNewProduct)
    }
  }
}

@ExperimentalFoundationApi
@Composable
fun ProductCard(product: Product, onClick: () -> Unit, onLongClick: () -> Unit) {
  Card(
          modifier = Modifier
              .width(206.dp)
              .height(216.dp)
              .combinedClickable(
                  onClick = onClick,
                  onLongClick = onLongClick
              ),
          elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
          shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Image
        if (product.image.isNotBlank()) {
            AsyncImage(
                model = product.image,
                contentDescription = product.name,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(product.name.take(2).uppercase(), style = MaterialTheme.typography.displayMedium)
            }
        }

        // Overlay (Blur/Gradient effect)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = product.name, 
                    style = MaterialTheme.typography.titleMedium, 
                    color = androidx.compose.ui.graphics.Color.White,
                    maxLines = 1
                )
                Text(
                    text = "$${product.price} / ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primaryContainer // Lightish color
                )
            }
        }
    }
  }
}

@Composable
fun NewProductCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp).height(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Add,
                    contentDescription = "Nuevo",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nuevo", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
