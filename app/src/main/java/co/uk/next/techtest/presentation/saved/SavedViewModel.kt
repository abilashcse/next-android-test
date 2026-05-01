package co.uk.next.techtest.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.repository.SavedProductsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(
    private val savedProductsRepository: SavedProductsRepository
) : ViewModel() {

    val items: StateFlow<List<ProductSummary>> =
        savedProductsRepository
            .observeSavedProducts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleFavourite(productId: Int) {
        val summary = items.value.firstOrNull { it.id == productId } ?: return
        viewModelScope.launch {
            savedProductsRepository.toggleSaved(summary)
        }
    }
}
