package co.uk.next.techtest.presentation.bag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.repository.BagProductsRepository
import co.uk.next.techtest.domain.repository.SavedProductsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BagViewModel(
    private val bagProductsRepository: BagProductsRepository,
    private val savedProductsRepository: SavedProductsRepository
) : ViewModel() {

    val items: StateFlow<List<ProductSummary>> =
        bagProductsRepository
            .observeBagProducts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favouriteIds: StateFlow<Set<Int>> =
        savedProductsRepository
            .observeSavedProductIds()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleFavourite(productId: Int) {
        val summary = items.value.firstOrNull { it.id == productId } ?: return
        viewModelScope.launch {
            savedProductsRepository.toggleSaved(summary)
        }
    }
}
