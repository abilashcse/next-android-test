package co.uk.next.techtest.domain.usecase

import co.uk.next.techtest.domain.model.ProductsPage
import co.uk.next.techtest.domain.repository.ProductsRepository

class SearchProductsUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke(query: String, limit: Int, skip: Int): Result<ProductsPage> =
        repository.searchProducts(query = query, limit = limit, skip = skip)
}
