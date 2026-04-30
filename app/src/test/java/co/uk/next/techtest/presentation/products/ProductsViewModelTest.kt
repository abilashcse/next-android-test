package co.uk.next.techtest.presentation.products

import co.uk.next.techtest.MainDispatcherRule
import co.uk.next.techtest.domain.model.ProductsPage
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.usecase.GetProductsPageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init loads products into state on success`() = runTest {
        val useCase = GetProductsPageUseCase(
            repository = object : co.uk.next.techtest.domain.repository.ProductsRepository {
                override suspend fun getProductsPage(limit: Int, skip: Int): Result<ProductsPage> =
                    Result.success(
                        ProductsPage(
                            items = listOf(
                                ProductSummary(
                                    id = 1,
                                    title = "Test",
                                    brand = "Brand",
                                    price = 10.0,
                                    thumbnailUrl = "https://example.com/a.jpg",
                                    discountPercentage = 10.0,
                                    rating = 4.5,
                                    stock = 124
                                )
                            ),
                            total = 100,
                            skip = 0,
                            limit = limit
                        )
                    )

                override suspend fun getProductDetails(id: Int) =
                    error("not used")
            }
        )

        val vm = ProductsViewModel(getProductsPage = useCase)

        val state = vm.uiState.value
        val success = state as ProductsListUiState.Success
        assertEquals(1, success.items.size)
        assertEquals(emptySet<Int>(), success.favouriteIds)
        assertEquals("Test", success.items.first().title)
    }

    @Test
    fun `init sets errorMessage on failure`() = runTest {
        val useCase = GetProductsPageUseCase(
            repository = object : co.uk.next.techtest.domain.repository.ProductsRepository {
                override suspend fun getProductsPage(limit: Int, skip: Int): Result<ProductsPage> =
                    Result.failure(IllegalStateException("boom"))

                override suspend fun getProductDetails(id: Int) =
                    error("not used")
            }
        )

        val vm = ProductsViewModel(getProductsPage = useCase)

        val state = vm.uiState.value
        val err = state as ProductsListUiState.Error
        assertNotNull(err.message)
    }

    @Test
    fun `loadNextPage appends items`() = runTest {
        var calls = 0
        val useCase = GetProductsPageUseCase(
            repository = object : co.uk.next.techtest.domain.repository.ProductsRepository {
                override suspend fun getProductsPage(limit: Int, skip: Int): Result<ProductsPage> {
                    calls++
                    return Result.success(
                        ProductsPage(
                            items = listOf(
                                ProductSummary(
                                    id = skip + 1,
                                    title = "Item ${skip + 1}",
                                    brand = null,
                                    price = null,
                                    thumbnailUrl = null,
                                    discountPercentage = null,
                                    rating = null,
                                    stock = null
                                )
                            ),
                            total = 2,
                            skip = skip,
                            limit = limit
                        )
                    )
                }

                override suspend fun getProductDetails(id: Int) =
                    error("not used")
            }
        )

        val vm = ProductsViewModel(getProductsPage = useCase)
        val first = vm.uiState.value as ProductsListUiState.Success
        assertEquals(1, first.items.size)

        vm.loadNextPage()

        val second = vm.uiState.value as ProductsListUiState.Success
        assertEquals(2, second.items.size)
        assertEquals(2, calls)
    }
}

