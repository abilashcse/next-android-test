package co.uk.next.techtest.presentation.productdetails

import co.uk.next.techtest.MainDispatcherRule
import co.uk.next.techtest.testsupport.FakeSavedProductsRepository
import co.uk.next.techtest.domain.model.Dimensions
import co.uk.next.techtest.domain.model.Meta
import co.uk.next.techtest.domain.model.ProductDetails
import co.uk.next.techtest.domain.model.ProductsPage
import co.uk.next.techtest.domain.model.Review
import co.uk.next.techtest.domain.usecase.GetProductDetailsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load sets success state on success`() = runTest {
        val useCase = GetProductDetailsUseCase(
            repository = object : co.uk.next.techtest.domain.repository.ProductsRepository {
                override suspend fun getProductsPage(limit: Int, skip: Int) =
                    error("not used")

                override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<ProductsPage> =
                    error("not used")

                override suspend fun getProductDetails(id: Int): Result<ProductDetails> =
                    Result.success(
                        ProductDetails(
                            id = id,
                            title = "Title",
                            description = "Desc",
                            price = 10.0,
                            discountPercentage = 0.0,
                            rating = 4.5,
                            stock = 12,
                            brand = "Brand",
                            category = "Cat",
                            thumbnailUrl = null,
                            imageUrls = emptyList(),
                            reviews = listOf(
                                Review(
                                    rating = 5,
                                    comment = "Great",
                                    date = "2024-01-01T00:00:00Z",
                                    reviewerName = "A",
                                    reviewerEmail = "a@example.com"
                                )
                            ),
                            returnPolicy = null,
                            shippingInformation = null,
                            availabilityStatus = null,
                            minimumOrderQuantity = null,
                            meta = Meta(
                                createdAt = null,
                                updatedAt = null,
                                barcode = null,
                                qrCode = null
                            ),
                            tags = emptyList(),
                            weight = null,
                            warrantyInformation = null,
                            dimensions = Dimensions(width = null, height = null, depth = null)
                        )
                    )
            }
        )

        val vm =
            ProductDetailsViewModel(
                getProductDetails = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )
        vm.load(productId = 123)
        advanceUntilIdle()

        val state = vm.uiState.value as ProductDetailsUiState.Success
        assertEquals(123, state.product.id)
        assertEquals("Title", state.product.title)
    }

    @Test
    fun `load sets error state on failure`() = runTest {
        val useCase = GetProductDetailsUseCase(
            repository = object : co.uk.next.techtest.domain.repository.ProductsRepository {
                override suspend fun getProductsPage(limit: Int, skip: Int) =
                    error("not used")

                override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<ProductsPage> =
                    error("not used")

                override suspend fun getProductDetails(id: Int): Result<ProductDetails> =
                    Result.failure(IllegalStateException("boom"))
            }
        )

        val vm =
            ProductDetailsViewModel(
                getProductDetails = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )
        vm.load(productId = 1)
        advanceUntilIdle()

        val state = vm.uiState.value as ProductDetailsUiState.Error
        assertTrue(state.message.isNotBlank())
    }
}

