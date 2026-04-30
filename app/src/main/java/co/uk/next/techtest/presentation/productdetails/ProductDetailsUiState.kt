package co.uk.next.techtest.presentation.productdetails

import co.uk.next.techtest.domain.model.ProductDetails

sealed interface ProductDetailsUiState {
    data object Loading : ProductDetailsUiState

    data class Success(
        val product: ProductDetails
    ) : ProductDetailsUiState

    data class Error(
        val message: String
    ) : ProductDetailsUiState
}

