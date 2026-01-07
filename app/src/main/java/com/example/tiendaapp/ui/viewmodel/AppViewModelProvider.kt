package com.example.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tiendaapp.TiendaApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(tiendaApplication().container.productRepository)
        }
        initializer {
            POSViewModel(
                tiendaApplication().container.productRepository,
                tiendaApplication().container.saleRepository
            )
        }
        initializer {
            ProductNewViewModel(tiendaApplication().container.productRepository)
        }
        initializer {
            HistoryViewModel(tiendaApplication().container.saleRepository)
        }
    }
}

fun CreationExtras.tiendaApplication(): TiendaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TiendaApplication)
