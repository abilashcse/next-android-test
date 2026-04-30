package co.uk.next.techtest.presentation.products.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.uk.next.techtest.domain.model.ProductSummary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun ProductResultsGrid(
    items: List<ProductSummary>,
    favouriteIds: Set<Int>,
    isAppending: Boolean,
    endReached: Boolean,
    columns: Int,
    onLoadNextPage: () -> Unit,
    onProductClick: (Int) -> Unit,
    onToggleFavourite: (Int) -> Unit,
    modifier: Modifier = Modifier,
    gridTestTag: String = "product_grid",
    gridPadding: Dp = 16.dp,
    gridSpacing: Dp = 16.dp
) {
    val gridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }

    LaunchedEffect(gridState, items.size, endReached, isAppending) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .map { it ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val thresholdIndex = (items.size - 6).coerceAtLeast(0)
                val shouldLoadMore = lastVisibleIndex >= thresholdIndex
                if (shouldLoadMore && !endReached && !isAppending) {
                    onLoadNextPage()
                }
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.testTag(gridTestTag),
        state = gridState,
        contentPadding = PaddingValues(gridPadding),
        horizontalArrangement = Arrangement.spacedBy(gridSpacing),
        verticalArrangement = Arrangement.spacedBy(gridSpacing)
    ) {
        items(
            items = items,
            key = { it.id }
        ) { product ->
            ProductTile(
                product = product,
                modifier = Modifier,
                isFavourite = favouriteIds.contains(product.id),
                onFavouriteClick = { onToggleFavourite(product.id) },
                onClick = { onProductClick(product.id) }
            )
        }

        if (isAppending) {
            item(
                key = "append_loading",
                span = { GridItemSpan(columns) }
            ) {
                Column(
                    modifier =
                        Modifier
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
