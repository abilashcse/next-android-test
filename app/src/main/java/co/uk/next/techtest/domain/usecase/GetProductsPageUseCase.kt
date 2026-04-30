package co.uk.next.techtest.domain.usecase

import co.uk.next.techtest.domain.model.ProductsPage
import co.uk.next.techtest.domain.repository.ProductsRepository

class GetProductsPageUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke(limit: Int, skip: Int): Result<ProductsPage> =
        repository.getProductsPage(limit = limit, skip = skip)
}

