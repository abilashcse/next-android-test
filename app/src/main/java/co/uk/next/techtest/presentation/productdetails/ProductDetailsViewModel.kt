package co.uk.next.techtest.presentation.productdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.next.techtest.domain.usecase.GetProductDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailsViewModel(
    private val getProductDetails: GetProductDetailsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailsUiState>(ProductDetailsUiState.Loading)
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    fun load(productId: Int) {
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
}

