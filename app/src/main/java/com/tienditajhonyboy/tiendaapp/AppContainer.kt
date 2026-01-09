package com.tienditajhonyboy.tiendaapp

import android.content.Context
import com.tienditajhonyboy.tiendaapp.data.local.AppDatabase
import com.tienditajhonyboy.tiendaapp.data.repository.ProductRepositoryImpl
import com.tienditajhonyboy.tiendaapp.data.repository.SaleRepositoryImpl
import com.tienditajhonyboy.tiendaapp.domain.repository.ProductRepository
import com.tienditajhonyboy.tiendaapp.domain.repository.SaleRepository

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
