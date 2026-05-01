package co.uk.next.techtest.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import androidx.compose.ui.window.DialogProperties
import co.uk.next.techtest.core.ui.shell.PlaceholderScreen
import co.uk.next.techtest.presentation.productdetails.ProductDetailsScreen
import co.uk.next.techtest.presentation.products.ProductsScreen
import co.uk.next.techtest.presentation.saved.SavedScreen
import co.uk.next.techtest.presentation.search.SearchScreen

@Composable
fun TechTestNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
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

        composable(Routes.Search) {
            SearchScreen(
                onProductClick = { id -> navController.navigate(Routes.productDetails(id)) }
            )
        }
        composable(Routes.Saved) {
            SavedScreen(
                onProductClick = { id -> navController.navigate(Routes.productDetails(id)) }
            )
        }
        composable(Routes.Bag) { PlaceholderScreen(title = "Bag") }
        composable(Routes.Account) { PlaceholderScreen(title = "Account") }

        dialog(
            route = Routes.ProductDetails,
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: return@dialog
            ProductDetailsScreen(
                productId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

