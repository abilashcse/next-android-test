package co.uk.next.techtest.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.repository.SavedProductsRepository
import co.uk.next.techtest.domain.usecase.GetProductsPageUseCase
import co.uk.next.techtest.presentation.products.prioritizeMajorSales
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val getProductsPage: GetProductsPageUseCase,
    private val savedProductsRepository: SavedProductsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductsListUiState>(ProductsListUiState.Loading)

    private val savedIds: StateFlow<Set<Int>> =
        savedProductsRepository
            .observeSavedProductIds()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val uiState: StateFlow<ProductsListUiState> =
        combine(_uiState, savedIds) { state, ids ->
            when (state) {
                is ProductsListUiState.Success -> state.copy(favouriteIds = ids)
                else -> state
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, _uiState.value)

    private val pageSize = 30
    private var total: Int? = null
    private var items: List<ProductSummary> = emptyList()
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
                items =
                    (if (isRefresh) page.items else (items + page.items)).prioritizeMajorSales()
                val endReached = items.size >= (total ?: 0)
                _uiState.value =
                    ProductsListUiState.Success(
                        items = items,
                        favouriteIds = savedIds.value,
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
        val summary = items.firstOrNull { it.id == productId } ?: return
        viewModelScope.launch {
            savedProductsRepository.toggleSaved(summary)
        }
    }
}
