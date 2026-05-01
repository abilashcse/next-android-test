package co.uk.next.techtest.presentation.products

import co.uk.next.techtest.MainDispatcherRule
import co.uk.next.techtest.domain.model.ProductsPage
import co.uk.next.techtest.testsupport.FakeSavedProductsRepository
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.usecase.GetProductsPageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

                override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<ProductsPage> =
                    error("not used")

                override suspend fun getProductDetails(id: Int) =
                    error("not used")
            }
        )

        val vm =
            ProductsViewModel(
                getProductsPage = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )

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

                override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<ProductsPage> =
                    error("not used")

                override suspend fun getProductDetails(id: Int) =
                    error("not used")
            }
        )

        val vm =
            ProductsViewModel(
                getProductsPage = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )

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

                override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<ProductsPage> =
                    error("not used")

                override suspend fun getProductDetails(id: Int) =
                    error("not used")
            }
        )

        val vm =
            ProductsViewModel(
                getProductsPage = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )
        val first = vm.uiState.value as ProductsListUiState.Success
        assertEquals(1, first.items.size)

        vm.loadNextPage()

        val second = vm.uiState.value as ProductsListUiState.Success
        assertEquals(2, second.items.size)
        assertEquals(2, calls)
    }

    @Test
    fun `toggleFavourite updates favouriteIds in success state`() = runTest {
        val useCase = GetProductsPageUseCase(
            repository = object : co.uk.next.techtest.domain.repository.ProductsRepository {
                override suspend fun getProductsPage(limit: Int, skip: Int): Result<ProductsPage> =
                    Result.success(
                        ProductsPage(
                            items = listOf(
                                ProductSummary(
                                    id = 10,
                                    title = "Test",
                                    brand = "Brand",
                                    price = 10.0,
                                    thumbnailUrl = "https://example.com/a.jpg",
                                    discountPercentage = 0.0,
                                    rating = 4.5,
                                    stock = 124
                                )
                            ),
                            total = 1,
                            skip = 0,
                            limit = limit
                        )
                    )

                override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<ProductsPage> =
                    error("not used")

                override suspend fun getProductDetails(id: Int) = error("not used")
            }
        )

        val vm =
            ProductsViewModel(
                getProductsPage = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )
        val before = vm.uiState.value as ProductsListUiState.Success
        assertEquals(emptySet<Int>(), before.favouriteIds)

        vm.toggleFavourite(10)

        val after = vm.uiState.value as ProductsListUiState.Success
        assertEquals(setOf(10), after.favouriteIds)

        vm.toggleFavourite(10)
        val afterSecondToggle = vm.uiState.value as ProductsListUiState.Success
        assertEquals(emptySet<Int>(), afterSecondToggle.favouriteIds)
    }

    @Test
    fun `loadNextPage does nothing when end reached`() = runTest {
        var calls = 0
        val useCase = GetProductsPageUseCase(
            repository = object : co.uk.next.techtest.domain.repository.ProductsRepository {
                override suspend fun getProductsPage(limit: Int, skip: Int): Result<ProductsPage> {
                    calls++
                    return Result.success(
                        ProductsPage(
                            items = listOf(
                                ProductSummary(
                                    id = 1,
                                    title = "Only",
                                    brand = null,
                                    price = null,
                                    thumbnailUrl = null,
                                    discountPercentage = null,
                                    rating = null,
                                    stock = null
                                )
                            ),
                            total = 1,
                            skip = skip,
                            limit = limit
                        )
                    )
                }

                override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<ProductsPage> =
                    error("not used")

                override suspend fun getProductDetails(id: Int) = error("not used")
            }
        )

        val vm =
            ProductsViewModel(
                getProductsPage = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )
        assertEquals(1, calls)

        vm.loadNextPage()
        advanceUntilIdle()

        assertEquals(1, calls)
    }

    @Test
    fun `loadNextPage failure keeps existing items in error state`() = runTest {
        var calls = 0
        val useCase = GetProductsPageUseCase(
            repository = object : co.uk.next.techtest.domain.repository.ProductsRepository {
                override suspend fun getProductsPage(limit: Int, skip: Int): Result<ProductsPage> {
                    calls++
                    return if (skip == 0) {
                        Result.success(
                            ProductsPage(
                                items = listOf(
                                    ProductSummary(
                                        id = 1,
                                        title = "First",
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
                    } else {
                        Result.failure(IllegalStateException("boom"))
                    }
                }

                override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<ProductsPage> =
                    error("not used")

                override suspend fun getProductDetails(id: Int) = error("not used")
            }
        )

        val vm =
            ProductsViewModel(
                getProductsPage = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )
        val first = vm.uiState.value as ProductsListUiState.Success
        assertEquals(listOf("First"), first.items.map { it.title })

        vm.loadNextPage()
        advanceUntilIdle()

        val err = vm.uiState.value as ProductsListUiState.Error
        assertTrue(err.message.isNotBlank())
        assertEquals(listOf("First"), err.items.map { it.title })
        assertEquals(2, calls)
    }
}

