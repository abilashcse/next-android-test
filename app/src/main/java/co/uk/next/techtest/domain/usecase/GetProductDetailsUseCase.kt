package co.uk.next.techtest.domain.usecase

import co.uk.next.techtest.domain.model.ProductDetails
import co.uk.next.techtest.domain.repository.ProductsRepository

class GetProductDetailsUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke(id: Int): Result<ProductDetails> = repository.getProductDetails(id)
}

