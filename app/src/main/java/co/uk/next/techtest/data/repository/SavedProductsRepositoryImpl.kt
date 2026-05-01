package co.uk.next.techtest.data.repository

import co.uk.next.techtest.data.local.SavedProductDao
import co.uk.next.techtest.data.local.SavedProductEntity
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.repository.SavedProductsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SavedProductsRepositoryImpl(
    private val dao: SavedProductDao
) : SavedProductsRepository {

    override fun observeSavedProductIds(): Flow<Set<Int>> =
        dao.observeAll().map { entities -> entities.map { it.id }.toSet() }

    override fun observeSavedProducts(): Flow<List<ProductSummary>> =
        dao.observeAll().map { entities -> entities.map { it.toSummary() } }

    override suspend fun toggleSaved(product: ProductSummary) {
        withContext(Dispatchers.IO) {
            if (dao.getById(product.id) != null) {
                dao.deleteById(product.id)
            } else {
                dao.insert(SavedProductEntity.from(product, savedAtMillis = System.currentTimeMillis()))
            }
        }
    }
}
