package co.uk.next.techtest.domain.repository

import co.uk.next.techtest.domain.model.ProductSummary
import kotlinx.coroutines.flow.Flow

interface BagProductsRepository {
    fun observeBagProductIds(): Flow<Set<Int>>

    fun observeBagProducts(): Flow<List<ProductSummary>>

    suspend fun toggleBag(product: ProductSummary)
}
