package co.uk.next.techtest.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.usecase.GetProductsPageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val getProductsPage: GetProductsPageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductsListUiState>(ProductsListUiState.Loading)
    val uiState: StateFlow<ProductsListUiState> = _uiState.asStateFlow()

    private val pageSize = 30
    private var total: Int? = null
    private var items: List<ProductSummary> = emptyList()
    private var favouriteIds: Set<Int> = emptySet()
    private var isRequestInFlight: Boolean = false

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = ProductsListUiState.Loading
        total = null
        items = emptyList()
        viewModelScope.launch {
            loadPage(skip = 0, isRefresh = true)
        }
    }

    fun loadNextPage() {
        val current = _uiState.value
        val endReached = total != null && items.size >= (total ?: 0)
        if (isRequestInFlight || endReached) return

        if (current is ProductsListUiState.Success) {
            _uiState.value = current.copy(isAppending = true)
        }

        viewModelScope.launch {
            loadPage(skip = items.size, isRefresh = false)
        }
    }

    private suspend fun loadPage(skip: Int, isRefresh: Boolean) {
        isRequestInFlight = true
        getProductsPage(limit = pageSize, skip = skip)
            .onSuccess { page ->
                total = page.total
                items = if (isRefresh) page.items else (items + page.items)
                val endReached = items.size >= (total ?: 0)
                _uiState.value =
                    ProductsListUiState.Success(
                        items = items,
                        favouriteIds = favouriteIds,
                        isAppending = false,
                        endReached = endReached
                    )
            }
            .onFailure { err ->
                _uiState.value =
                    ProductsListUiState.Error(
                        message = err.message ?: "Something went wrong",
                        items = items
                    )
            }
        isRequestInFlight = false
    }

    fun toggleFavourite(productId: Int) {
        favouriteIds =
            if (favouriteIds.contains(productId)) favouriteIds - productId else favouriteIds + productId

        val current = _uiState.value
        if (current is ProductsListUiState.Success) {
            _uiState.value = current.copy(favouriteIds = favouriteIds)
        }
    }
}

