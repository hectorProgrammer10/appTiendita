package com.tienditajhonyboy.tiendaapp.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tienditajhonyboy.tiendaapp.TiendaApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                tiendaApplication().container.productRepository,
                tiendaApplication().container.workspaceRepository
            )
        }
        initializer {
            POSViewModel(
                tiendaApplication().container.productRepository,
                tiendaApplication().container.saleRepository,
                tiendaApplication().container.workspaceRepository
            )
        }
        initializer {
            ProductNewViewModel(
                tiendaApplication().container.productRepository,
                tiendaApplication().container.workspaceRepository
            )
        }
        initializer {
            HistoryViewModel(
                tiendaApplication().container.saleRepository,
                tiendaApplication().container.workspaceRepository
            )
        }
        initializer {
            ProductEditViewModel(tiendaApplication().container.productRepository)
        }
    }
}


fun CreationExtras.tiendaApplication(): TiendaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TiendaApplication)
