package co.uk.next.techtest.presentation.search

import co.uk.next.techtest.domain.model.ProductSummary

sealed interface SearchUiState {
    data object Idle : SearchUiState

    data object Loading : SearchUiState

    data class Success(
        val activeQuery: String,
        val items: List<ProductSummary>,
        val favouriteIds: Set<Int>,
        val isAppending: Boolean,
        val endReached: Boolean
    ) : SearchUiState

    data class Error(
        val message: String,
        val activeQuery: String
    ) : SearchUiState
}
