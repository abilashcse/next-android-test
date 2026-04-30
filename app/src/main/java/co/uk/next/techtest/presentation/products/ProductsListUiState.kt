package co.uk.next.techtest.presentation.products

import co.uk.next.techtest.domain.model.ProductSummary

sealed interface ProductsListUiState {
    data object Loading : ProductsListUiState

    data class Success(
        val items: List<ProductSummary>,
        val favouriteIds: Set<Int>,
        val isAppending: Boolean,
        val endReached: Boolean
    ) : ProductsListUiState

    data class Error(
        val message: String,
        val items: List<ProductSummary> = emptyList()
    ) : ProductsListUiState
}

