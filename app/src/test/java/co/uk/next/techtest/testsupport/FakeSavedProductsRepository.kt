package co.uk.next.techtest.testsupport

import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.repository.SavedProductsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSavedProductsRepository(
    initial: List<ProductSummary> = emptyList()
) : SavedProductsRepository {

    private val _saved = MutableStateFlow(initial)

    override fun observeSavedProductIds(): Flow<Set<Int>> =
        _saved.map { summaries -> summaries.map { it.id }.toSet() }

    override fun observeSavedProducts(): Flow<List<ProductSummary>> = _saved

    override suspend fun toggleSaved(product: ProductSummary) {
        val current = _saved.value.toMutableList()
        val idx = current.indexOfFirst { it.id == product.id }
        if (idx >= 0) {
            current.removeAt(idx)
        } else {
            current.add(0, product)
        }
        _saved.value = current
    }
}
