package com.tienditajhonyboy.tiendaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.tienditajhonyboy.tiendaapp.ui.navigation.AppNavigation
import com.tienditajhonyboy.tiendaapp.ui.theme.TiendaAppTheme
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val _externalIntentUri = MutableStateFlow<android.net.Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)
        
        setContent {
            val externalUri by _externalIntentUri.collectAsState()
            
            TiendaAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        externalUri = externalUri,
                        onExternalUriConsumed = { _externalIntentUri.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Usamos intent, no this.intent porque el parámetro oculta la propiedad si el nombre es igual, 
        // pero la firma estándar en kotlin para onNewIntent es (intent: Intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            _externalIntentUri.value = intent.data
        }
    }
}
