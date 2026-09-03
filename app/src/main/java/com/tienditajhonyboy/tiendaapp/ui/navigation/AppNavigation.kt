package com.tienditajhonyboy.tiendaapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tienditajhonyboy.tiendaapp.ui.screens.HistoryScreen
import com.tienditajhonyboy.tiendaapp.ui.screens.HomeScreen
import com.tienditajhonyboy.tiendaapp.ui.screens.POSScreen
import com.tienditajhonyboy.tiendaapp.ui.screens.ProductNewScreen
import com.tienditajhonyboy.tiendaapp.ui.screens.ProductEditScreen

enum class AppDestinations(val route: String) {
    Home("home"),
    POS("pos"),
    ProductNew("product_new"),
    ProductEdit("product_edit"),
    History("history"),
    Agent("agent")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    externalUri: android.net.Uri? = null,
    onExternalUriConsumed: () -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(externalUri) {
        if (externalUri != null) {
            val mimeType = context.contentResolver.getType(externalUri) ?: ""
            val path = externalUri.path ?: ""
            val encodedUri = java.net.URLEncoder.encode(externalUri.toString(), "UTF-8")

            if (mimeType.contains("json") || path.endsWith(".json", ignoreCase = true)) {
                navController.navigate("home?importUri=$encodedUri") {
                    launchSingleTop = true
                }
            } else {
                navController.navigate("history?importUri=$encodedUri") {
                    launchSingleTop = true
                }
            }
            onExternalUriConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestinations.Home.route,
        modifier = modifier
    ) {
        composable(
            route = "home?importUri={importUri}",
            arguments = listOf(navArgument("importUri") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val importUriString = backStackEntry.arguments?.getString("importUri")
            val alreadyConsumed = backStackEntry.savedStateHandle.get<Boolean>("importUriConsumed") == true
            val importUri = if (!alreadyConsumed && importUriString != null) {
                backStackEntry.savedStateHandle["importUriConsumed"] = true
                android.net.Uri.parse(java.net.URLDecoder.decode(importUriString, "UTF-8"))
            } else null

            HomeScreen(
                onNavigateToPOS = { productId ->
                    val route = if (productId != null) "pos?productId=$productId" else "pos"
                    navController.navigate(route)
                },
                onNavigateToNewProduct = { navController.navigate(AppDestinations.ProductNew.route) },
                onNavigateToEditProduct = { productId -> navController.navigate("${AppDestinations.ProductEdit.route}/$productId") },
                onNavigateToHistory = { navController.navigate(AppDestinations.History.route) },
                onNavigateToAgent = { navController.navigate(AppDestinations.Agent.route) },
                initialImportUri = importUri
            )
        }
        composable(
            route = "pos?productId={productId}",
            arguments = listOf(navArgument("productId") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            })
        ) { backStackEntry ->
            POSScreen(
                onNavigateBack = { navController.popBackStack() },
                productIdToSelect = backStackEntry.arguments?.getString("productId")
            )
        }
        composable(AppDestinations.ProductNew.route) {
            ProductNewScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "history?importUri={importUri}",
            arguments = listOf(navArgument("importUri") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val importUriString = backStackEntry.arguments?.getString("importUri")
            val alreadyConsumed = backStackEntry.savedStateHandle.get<Boolean>("importUriConsumed") == true
            val importUri = if (!alreadyConsumed && importUriString != null) {
                backStackEntry.savedStateHandle["importUriConsumed"] = true
                android.net.Uri.parse(java.net.URLDecoder.decode(importUriString, "UTF-8"))
            } else null
            
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                initialImportUri = importUri
            )
        }
        composable(
            route = "${AppDestinations.ProductEdit.route}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductEditScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(AppDestinations.Agent.route) {
            com.tienditajhonyboy.tiendaapp.ui.screens.AgentScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
