package com.tienditajhonyboy.tiendaapp.ui.navigation

import androidx.compose.runtime.Composable
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
    History("history")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.Home.route,
        modifier = modifier
    ) {
        composable(AppDestinations.Home.route) {
            HomeScreen(
                onNavigateToPOS = { productId ->
                    val route = if (productId != null) "pos?productId=$productId" else "pos"
                    navController.navigate(route)
                },
                onNavigateToNewProduct = { navController.navigate(AppDestinations.ProductNew.route) },
                onNavigateToEditProduct = { productId -> navController.navigate("${AppDestinations.ProductEdit.route}/$productId") },
                onNavigateToHistory = { navController.navigate(AppDestinations.History.route) }
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
        composable(AppDestinations.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
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
    }
}
