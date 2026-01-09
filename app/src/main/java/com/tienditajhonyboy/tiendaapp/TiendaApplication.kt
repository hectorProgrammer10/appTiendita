package com.tienditajhonyboy.tiendaapp

import android.app.Application

class TiendaApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
