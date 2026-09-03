package com.tienditajhonyboy.tiendaapp

import android.content.Context
import com.tienditajhonyboy.tiendaapp.data.local.AppDatabase
import com.tienditajhonyboy.tiendaapp.data.repository.ProductRepositoryImpl
import com.tienditajhonyboy.tiendaapp.data.repository.SaleRepositoryImpl
import com.tienditajhonyboy.tiendaapp.data.repository.WorkspaceRepositoryImpl
import com.tienditajhonyboy.tiendaapp.data.local.agent.AgentDatabase
import com.tienditajhonyboy.tiendaapp.data.repository.AgentRepositoryImpl
import com.tienditajhonyboy.tiendaapp.domain.repository.AgentRepository
import com.tienditajhonyboy.tiendaapp.domain.repository.ProductRepository
import com.tienditajhonyboy.tiendaapp.domain.repository.SaleRepository
import com.tienditajhonyboy.tiendaapp.domain.repository.WorkspaceRepository

interface AppContainer {
    val productRepository: ProductRepository
    val saleRepository: SaleRepository
    val workspaceRepository: WorkspaceRepository
    val agentRepository: AgentRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(AppDatabase.getDatabase(context).productDao())
    }
    override val saleRepository: SaleRepository by lazy {
        SaleRepositoryImpl(AppDatabase.getDatabase(context).saleDao())
    }
    override val workspaceRepository: WorkspaceRepository by lazy {
        WorkspaceRepositoryImpl(AppDatabase.getDatabase(context).workspaceDao(), context)
    }
    override val agentRepository: AgentRepository by lazy {
        AgentRepositoryImpl(AgentDatabase.getDatabase(context).agentDao())
    }
}

