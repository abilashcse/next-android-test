package co.uk.next.techtest.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import co.uk.next.techtest.R
import co.uk.next.techtest.presentation.products.components.ProductResultsGrid
import co.uk.next.techtest.presentation.products.components.gridColumnCount
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onProductClick: (Int) -> Unit = {},
    viewModel: SearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val queryDraft by viewModel.queryDraft.collectAsState()

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

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = queryDraft,
            onValueChange = viewModel::onQueryChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_field"),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (queryDraft.isNotEmpty()) {
                    IconButton(onClick = viewModel::clearSearch) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.search_clear_content_description)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions =
                KeyboardActions(
                    onSearch = { viewModel.submitSearch() }
                )
        )

        when (val state = uiState) {
            is SearchUiState.Idle -> {
                Text(
                    text = stringResource(R.string.search_idle_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .testTag("search_idle_hint")
                )
            }

            is SearchUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.testTag("search_loading"))
                }
            }

            is SearchUiState.Error -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("search_error"),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = viewModel::retryLastSearch,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(text = stringResource(R.string.search_error_retry))
                    }
                }
            }

            is SearchUiState.Success -> {
                if (state.items.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_no_results, state.activeQuery),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .testTag("search_no_results")
                    )
                } else {
                    ProductResultsGrid(
                        items = state.items,
                        favouriteIds = state.favouriteIds,
                        isAppending = state.isAppending,
                        endReached = state.endReached,
                        columns = columns,
                        onLoadNextPage = viewModel::loadNextPage,
                        onProductClick = onProductClick,
                        onToggleFavourite = viewModel::toggleFavourite,
                        modifier = Modifier.fillMaxSize(),
                        gridTestTag = "search_grid",
                        gridPadding = gridPadding,
                        gridSpacing = gridSpacing
                    )
                }
            }
        }
    }
}
