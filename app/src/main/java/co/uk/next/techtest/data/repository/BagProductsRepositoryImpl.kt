package co.uk.next.techtest.data.repository

import co.uk.next.techtest.data.local.BagProductDao
import co.uk.next.techtest.data.local.BagProductEntity
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.repository.BagProductsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BagProductsRepositoryImpl(
    private val dao: BagProductDao
) : BagProductsRepository {

    override fun observeBagProductIds(): Flow<Set<Int>> =
        dao.observeAll().map { entities -> entities.map { it.id }.toSet() }

    override fun observeBagProducts(): Flow<List<ProductSummary>> =
        dao.observeAll().map { entities -> entities.map { it.toSummary() } }

    override suspend fun toggleBag(product: ProductSummary) {
        withContext(Dispatchers.IO) {
            if (dao.getById(product.id) != null) {
                dao.deleteById(product.id)
            } else {
                dao.insert(
                    BagProductEntity.from(
                        product,
                        addedAtMillis = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
