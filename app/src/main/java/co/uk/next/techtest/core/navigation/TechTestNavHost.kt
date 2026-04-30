package co.uk.next.techtest.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.uk.next.techtest.presentation.productdetails.ProductDetailsScreen
import co.uk.next.techtest.presentation.products.ProductsScreen

@Composable
fun TechTestNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Products,
        modifier = modifier
    ) {
        composable(Routes.Products) {
            ProductsScreen(
                onProductClick = { id -> navController.navigate(Routes.productDetails(id)) }
            )
        }

        composable(
            route = Routes.ProductDetails,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: return@composable
            ProductDetailsScreen(
                productId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

