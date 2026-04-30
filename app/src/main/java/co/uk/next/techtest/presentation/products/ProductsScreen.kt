package co.uk.next.techtest.presentation.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import co.uk.next.techtest.presentation.products.components.ProductTile
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    modifier: Modifier = Modifier,
    onProductClick: (Int) -> Unit = {},
    viewModel: ProductsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab = rememberSaveable { mutableStateOf(HomeTab.Home) }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Next Tech Test") },
                actions = {
                    IconButton(onClick = { /* cart placeholder */ }) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Cart"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab.value == tab,
                        onClick = { selectedTab.value = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is ProductsListUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProductsListUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(onClick = viewModel::refresh) {
                        Text(text = "Retry")
                    }
                }
            }

            is ProductsListUiState.Success -> {
                // Only Home tab is wired for now.
                if (selectedTab.value != HomeTab.Home) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${selectedTab.value.label} (coming soon)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    return@Scaffold
                }

                val configuration = LocalConfiguration.current
                val gridPadding = 16.dp
                val gridSpacing = 16.dp
                val minTileWidth = if (configuration.screenWidthDp < 360) 160.dp else 180.dp
                val columns = rememberGridColumns(
                    screenWidthDp = configuration.screenWidthDp,
                    horizontalPadding = gridPadding,
                    horizontalSpacing = gridSpacing,
                    minTileWidth = minTileWidth,
                    minColumns = 2,
                    maxColumns = 4
                )
                val gridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }

                LaunchedEffect(gridState, state.items.size, state.endReached, state.isAppending) {
                    snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .map { it ?: 0 }
                        .distinctUntilChanged()
                        .collect { lastVisibleIndex ->
                            val thresholdIndex = (state.items.size - 6).coerceAtLeast(0)
                            val shouldLoadMore = lastVisibleIndex >= thresholdIndex
                            if (shouldLoadMore && !state.endReached && !state.isAppending) {
                                viewModel.loadNextPage()
                            }
                        }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.padding(innerPadding),
                    state = gridState,
                    contentPadding = PaddingValues(gridPadding),
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                    verticalArrangement = Arrangement.spacedBy(gridSpacing)
                ) {
                    items(
                        items = state.items,
                        key = { it.id }
                    ) { product ->
                        ProductTile(
                            product = product,
                            modifier = Modifier,
                            isFavourite = state.favouriteIds.contains(product.id),
                            onFavouriteClick = { viewModel.toggleFavourite(product.id) },
                            onClick = { onProductClick(product.id) }
                        )
                    }

                    if (state.isAppending) {
                        item(
                            key = "append_loading",
                            span = { GridItemSpan(columns) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class HomeTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Outlined.Home),
    Search("Search", Icons.Outlined.Search),
    Saved("Saved", Icons.Outlined.BookmarkBorder),
    Bag("Bag", Icons.Outlined.ShoppingBag),
    Account("Account", Icons.Outlined.AccountCircle);
}

private fun rememberGridColumns(
    screenWidthDp: Int,
    horizontalPadding: Dp,
    horizontalSpacing: Dp,
    minTileWidth: Dp,
    minColumns: Int,
    maxColumns: Int
): Int {
    val available = screenWidthDp.dp - (horizontalPadding * 2)
    // columns = floor((available + spacing) / (tileWidth + spacing))
    val ratio: Float = (available + horizontalSpacing) / (minTileWidth + horizontalSpacing)
    val raw = floor(ratio.toDouble()).toInt()

    return raw.coerceIn(minColumns, maxColumns)
}

