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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import co.uk.next.techtest.presentation.products.components.ProductTile
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.floor

@Composable
fun ProductsScreen(
    modifier: Modifier = Modifier,
    onProductClick: (Int) -> Unit = {},
    viewModel: ProductsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ProductsListUiState.Loading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(modifier = Modifier.testTag("plp_loading"))
            }
        }

        is ProductsListUiState.Error -> {
            Column(
                modifier = modifier.fillMaxSize(),
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
                modifier = modifier.testTag("plp_grid"),
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
                                .padding(vertical = 8.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
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

