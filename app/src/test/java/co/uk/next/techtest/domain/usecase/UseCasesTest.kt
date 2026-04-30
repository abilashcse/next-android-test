package co.uk.next.techtest.domain.usecase

import co.uk.next.techtest.domain.model.ProductDetails
import co.uk.next.techtest.domain.model.ProductsPage
import co.uk.next.techtest.domain.repository.ProductsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UseCasesTest {

    @Test
    fun `GetProductsPageUseCase delegates to repository and returns result`() = runTest {
        var seenLimit: Int? = null
        var seenSkip: Int? = null
        val expected = ProductsPage(items = emptyList(), total = 1, skip = 2, limit = 3)

        val repo =
            object : ProductsRepository {
                override suspend fun getProductsPage(limit: Int, skip: Int): Result<ProductsPage> {
                    seenLimit = limit
                    seenSkip = skip
                    return Result.success(expected)
                }

                override suspend fun getProductDetails(id: Int): Result<ProductDetails> =
                    error("not used")
            }

        val useCase = GetProductsPageUseCase(repository = repo)
        val result = useCase(limit = 3, skip = 2)

        assertEquals(3, seenLimit)
        assertEquals(2, seenSkip)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `GetProductDetailsUseCase delegates to repository and returns result`() = runTest {
        var seenId: Int? = null
        val expected =
            ProductDetails(
                id = 99,
                title = "T",
                description = null,
                price = null,
                discountPercentage = null,
                rating = null,
                stock = null,
                brand = null,
                category = null,
                thumbnailUrl = null,
                imageUrls = emptyList(),
                reviews = emptyList(),
                returnPolicy = null,
                shippingInformation = null,
                availabilityStatus = null,
                minimumOrderQuantity = null,
                meta = null,
                tags = emptyList(),
                weight = null,
                warrantyInformation = null,
                dimensions = null
            )

        val repo =
            object : ProductsRepository {
                override suspend fun getProductsPage(limit: Int, skip: Int): Result<ProductsPage> =
                    error("not used")

                override suspend fun getProductDetails(id: Int): Result<ProductDetails> {
                    seenId = id
                    return Result.success(expected)
                }
            }

        val useCase = GetProductDetailsUseCase(repository = repo)
        val result = useCase(id = 99)

        assertEquals(99, seenId)
        assertEquals(expected, result.getOrNull())
    }
}

