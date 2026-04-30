package co.uk.next.techtest.core.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector
import co.uk.next.techtest.core.navigation.Routes

enum class BottomNavDestination(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    Home("Home", Icons.Outlined.Home, Routes.Products),
    Search("Search", Icons.Outlined.Search, Routes.Search),
    Saved("Saved", Icons.Outlined.BookmarkBorder, Routes.Saved),
    Bag("Bag", Icons.Outlined.ShoppingBag, Routes.Bag),
    Account("Account", Icons.Outlined.AccountCircle, Routes.Account)
}

