package co.uk.next.techtest.presentation.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.uk.next.techtest.R
import co.uk.next.techtest.presentation.products.components.ProductResultsGrid
import co.uk.next.techtest.presentation.products.components.gridColumnCount
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SavedScreen(
    modifier: Modifier = Modifier,
    onProductClick: (Int) -> Unit = {},
    viewModel: SavedViewModel = koinViewModel()
) {
    val items by viewModel.items.collectAsState()
    val favouriteIds = items.map { it.id }.toSet()

    if (items.isEmpty()) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .testTag("saved_empty"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.saved_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.saved_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        return
    }

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
        items = items,
        favouriteIds = favouriteIds,
        isAppending = false,
        endReached = true,
        columns = columns,
        onLoadNextPage = {},
        onProductClick = onProductClick,
        onToggleFavourite = viewModel::toggleFavourite,
        modifier = modifier.fillMaxSize(),
        gridTestTag = "saved_grid",
        gridPadding = gridPadding,
        gridSpacing = gridSpacing
    )
}
