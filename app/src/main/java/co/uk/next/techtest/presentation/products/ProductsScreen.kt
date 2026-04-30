package co.uk.next.techtest.presentation.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import co.uk.next.techtest.presentation.products.components.ProductResultsGrid
import co.uk.next.techtest.presentation.products.components.gridColumnCount
import org.koin.compose.viewmodel.koinViewModel

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
            val columns =
                gridColumnCount(
                    screenWidthDp = configuration.screenWidthDp,
                    horizontalPadding = gridPadding,
                    horizontalSpacing = gridSpacing,
                    minTileWidth = minTileWidth,
                    minColumns = 2,
                    maxColumns = 4
                )

            ProductResultsGrid(
                items = state.items,
                favouriteIds = state.favouriteIds,
                isAppending = state.isAppending,
                endReached = state.endReached,
                columns = columns,
                onLoadNextPage = viewModel::loadNextPage,
                onProductClick = onProductClick,
                onToggleFavourite = viewModel::toggleFavourite,
                modifier = modifier.fillMaxSize(),
                gridTestTag = "plp_grid",
                gridPadding = gridPadding,
                gridSpacing = gridSpacing
            )
        }
    }
}
