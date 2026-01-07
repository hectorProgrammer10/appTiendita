package com.example.tiendaapp

import android.content.Context
import com.example.tiendaapp.data.local.AppDatabase
import com.example.tiendaapp.data.repository.ProductRepositoryImpl
import com.example.tiendaapp.data.repository.SaleRepositoryImpl
import com.example.tiendaapp.domain.repository.ProductRepository
import com.example.tiendaapp.domain.repository.SaleRepository

interface AppContainer {
    val productRepository: ProductRepository
    val saleRepository: SaleRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(AppDatabase.getDatabase(context).productDao())
    }
    override val saleRepository: SaleRepository by lazy {
        SaleRepositoryImpl(AppDatabase.getDatabase(context).saleDao())
    }
}
