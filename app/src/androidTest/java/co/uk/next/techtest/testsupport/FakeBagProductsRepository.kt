package co.uk.next.techtest.testsupport

import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.repository.BagProductsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeBagProductsRepository(
    initial: List<ProductSummary> = emptyList()
) : BagProductsRepository {

    private val _bag = MutableStateFlow(initial)

    override fun observeBagProductIds(): Flow<Set<Int>> =
        _bag.map { summaries -> summaries.map { it.id }.toSet() }

    override fun observeBagProducts(): Flow<List<ProductSummary>> = _bag

    override suspend fun toggleBag(product: ProductSummary) {
        val current = _bag.value.toMutableList()
        val idx = current.indexOfFirst { it.id == product.id }
        if (idx >= 0) {
            current.removeAt(idx)
        } else {
            current.add(0, product)
        }
        _bag.value = current
    }
}
