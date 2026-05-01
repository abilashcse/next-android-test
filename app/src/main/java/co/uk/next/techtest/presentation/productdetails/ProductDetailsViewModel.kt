package co.uk.next.techtest.presentation.productdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.next.techtest.domain.model.toProductSummary
import co.uk.next.techtest.domain.repository.SavedProductsRepository
import co.uk.next.techtest.domain.usecase.GetProductDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    private val getProductDetails: GetProductDetailsUseCase,
    private val savedProductsRepository: SavedProductsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailsUiState>(ProductDetailsUiState.Loading)
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    private val activeProductId = MutableStateFlow<Int?>(null)

    val isSaved: StateFlow<Boolean> =
        combine(
            savedProductsRepository.observeSavedProductIds(),
            activeProductId
        ) { ids, productId ->
            productId != null && productId in ids
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun load(productId: Int) {
        activeProductId.value = productId
        _uiState.value = ProductDetailsUiState.Loading
        viewModelScope.launch {
            getProductDetails(productId)
                .onSuccess { product ->
                    _uiState.value = ProductDetailsUiState.Success(product = product)
                }
                .onFailure { err ->
                    _uiState.value =
                        ProductDetailsUiState.Error(message = err.message ?: "Something went wrong")
                }
        }
    }

    fun toggleSaved() {
        val product = (_uiState.value as? ProductDetailsUiState.Success)?.product ?: return
        viewModelScope.launch {
            savedProductsRepository.toggleSaved(product.toProductSummary())
        }
    }
}
