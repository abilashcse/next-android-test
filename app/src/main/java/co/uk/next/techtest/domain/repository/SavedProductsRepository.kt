package co.uk.next.techtest.domain.repository

import co.uk.next.techtest.domain.model.ProductSummary
import kotlinx.coroutines.flow.Flow

interface SavedProductsRepository {
    fun observeSavedProductIds(): Flow<Set<Int>>

    fun observeSavedProducts(): Flow<List<ProductSummary>>

    suspend fun toggleSaved(product: ProductSummary)
}
