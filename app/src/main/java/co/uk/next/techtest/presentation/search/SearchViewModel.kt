package co.uk.next.techtest.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.usecase.SearchProductsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchProducts: SearchProductsUseCase
) : ViewModel() {

    private val _queryDraft = MutableStateFlow("")
    val queryDraft: StateFlow<String> = _queryDraft.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val pageSize = 30
    private var total: Int? = null
    private var items: List<ProductSummary> = emptyList()
    private var favouriteIds: Set<Int> = emptySet()
    private var isRequestInFlight: Boolean = false
    private var activeQuery: String = ""
    private var requestGeneration: Int = 0

    private var debounceJob: Job? = null
    private val debounceMs = 350L

    fun onQueryChange(raw: String) {
        _queryDraft.value = raw
        debounceJob?.cancel()
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            requestGeneration++
            activeQuery = ""
            total = null
            items = emptyList()
            _uiState.value = SearchUiState.Idle
            return
        }
        debounceJob =
            viewModelScope.launch {
                delay(debounceMs)
                runSearch(trimmed)
            }
    }

    fun submitSearch() {
        debounceJob?.cancel()
        val trimmed = _queryDraft.value.trim()
        if (trimmed.isEmpty()) {
            requestGeneration++
            activeQuery = ""
            total = null
            items = emptyList()
            _uiState.value = SearchUiState.Idle
            return
        }
        viewModelScope.launch {
            runSearch(trimmed)
        }
    }

    fun clearSearch() {
        debounceJob?.cancel()
        _queryDraft.value = ""
        requestGeneration++
        activeQuery = ""
        total = null
        items = emptyList()
        _uiState.value = SearchUiState.Idle
    }

    fun loadNextPage() {
        val current = _uiState.value
        val endReached = total != null && items.size >= (total ?: 0)
        if (isRequestInFlight || endReached || activeQuery.isEmpty()) return

        if (current is SearchUiState.Success) {
            _uiState.value = current.copy(isAppending = true)
        }

        viewModelScope.launch {
            loadPage(skip = items.size, isRefresh = false)
        }
    }

    fun retryLastSearch() {
        val q =
            when (val s = _uiState.value) {
                is SearchUiState.Error -> s.activeQuery
                else -> activeQuery.ifEmpty { _queryDraft.value.trim() }
            }
        if (q.isEmpty()) return
        viewModelScope.launch {
            runSearch(q)
        }
    }

    fun toggleFavourite(productId: Int) {
        favouriteIds =
            if (favouriteIds.contains(productId)) favouriteIds - productId else favouriteIds + productId

        val current = _uiState.value
        if (current is SearchUiState.Success) {
            _uiState.value = current.copy(favouriteIds = favouriteIds)
        }
    }

    private suspend fun runSearch(trimmed: String) {
        val gen = ++requestGeneration
        activeQuery = trimmed
        _uiState.value = SearchUiState.Loading
        total = null
        items = emptyList()
        loadPageInternal(trimmed, skip = 0, isRefresh = true, generation = gen)
    }

    private suspend fun loadPage(skip: Int, isRefresh: Boolean) {
        loadPageInternal(activeQuery, skip = skip, isRefresh = isRefresh, generation = requestGeneration)
    }

    private suspend fun loadPageInternal(
        query: String,
        skip: Int,
        isRefresh: Boolean,
        generation: Int
    ) {
        isRequestInFlight = true
        searchProducts(query, pageSize, skip)
            .onSuccess { page ->
                if (generation != requestGeneration) return@onSuccess
                total = page.total
                items = if (isRefresh) page.items else (items + page.items)
                val endReached = items.size >= (total ?: 0)
                _uiState.value =
                    SearchUiState.Success(
                        activeQuery = query,
                        items = items,
                        favouriteIds = favouriteIds,
                        isAppending = false,
                        endReached = endReached
                    )
            }
            .onFailure { err ->
                if (generation != requestGeneration) return@onFailure
                _uiState.value =
                    SearchUiState.Error(
                        message = err.message ?: "Something went wrong",
                        activeQuery = query
                    )
            }
        isRequestInFlight = false
    }
}
