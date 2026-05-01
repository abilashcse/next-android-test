package co.uk.next.techtest.presentation.search

import co.uk.next.techtest.MainDispatcherRule
import co.uk.next.techtest.domain.model.ProductsPage
import co.uk.next.techtest.testsupport.FakeSavedProductsRepository
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.usecase.SearchProductsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `blank query after trim shows idle`() = runTest {
        val useCase =
            SearchProductsUseCase(
                repository =
                    object : co.uk.next.techtest.domain.repository.ProductsRepository {
                        override suspend fun getProductsPage(limit: Int, skip: Int) =
                            error("not used")

                        override suspend fun searchProducts(
                            query: String,
                            limit: Int,
                            skip: Int
                        ): Result<ProductsPage> = error("not used")

                        override suspend fun getProductDetails(id: Int) = error("not used")
                    }
            )
        val vm =
            SearchViewModel(
                searchProducts = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )
        vm.onQueryChange("   ")
        advanceUntilIdle()
        assertEquals(SearchUiState.Idle, vm.uiState.value)
    }

    @Test
    fun `debounced search loads success`() = runTest {
        val useCase =
            SearchProductsUseCase(
                repository =
                    object : co.uk.next.techtest.domain.repository.ProductsRepository {
                        override suspend fun getProductsPage(limit: Int, skip: Int) =
                            error("not used")

                        override suspend fun searchProducts(
                            query: String,
                            limit: Int,
                            skip: Int
                        ): Result<ProductsPage> =
                            Result.success(
                                ProductsPage(
                                    items =
                                        listOf(
                                            ProductSummary(
                                                id = 1,
                                                title = "A",
                                                brand = null,
                                                price = 1.0,
                                                thumbnailUrl = null,
                                                discountPercentage = null,
                                                rating = null,
                                                stock = null
                                            )
                                        ),
                                    total = 1,
                                    skip = 0,
                                    limit = 30
                                )
                            )

                        override suspend fun getProductDetails(id: Int) = error("not used")
                    }
            )
        val vm =
            SearchViewModel(
                searchProducts = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )
        vm.onQueryChange("phone")
        advanceTimeBy(400)
        advanceUntilIdle()

        val s = vm.uiState.value as SearchUiState.Success
        assertEquals("phone", s.activeQuery)
        assertEquals(1, s.items.size)
    }

    @Test
    fun `empty results shows success with empty items`() = runTest {
        val useCase =
            SearchProductsUseCase(
                repository =
                    object : co.uk.next.techtest.domain.repository.ProductsRepository {
                        override suspend fun getProductsPage(limit: Int, skip: Int) =
                            error("not used")

                        override suspend fun searchProducts(
                            query: String,
                            limit: Int,
                            skip: Int
                        ): Result<ProductsPage> =
                            Result.success(
                                ProductsPage(items = emptyList(), total = 0, skip = 0, limit = 30)
                            )

                        override suspend fun getProductDetails(id: Int) = error("not used")
                    }
            )
        val vm =
            SearchViewModel(
                searchProducts = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )
        vm.onQueryChange("jeans")
        advanceTimeBy(400)
        advanceUntilIdle()

        val s = vm.uiState.value as SearchUiState.Success
        assertTrue(s.items.isEmpty())
        assertTrue(s.endReached)
    }

    @Test
    fun `failure shows error and retry reruns search`() = runTest {
        var calls = 0
        val useCase =
            SearchProductsUseCase(
                repository =
                    object : co.uk.next.techtest.domain.repository.ProductsRepository {
                        override suspend fun getProductsPage(limit: Int, skip: Int) =
                            error("not used")

                        override suspend fun searchProducts(
                            query: String,
                            limit: Int,
                            skip: Int
                        ): Result<ProductsPage> {
                            calls++
                            return if (calls == 1) {
                                Result.failure(IllegalStateException("server error"))
                            } else {
                                Result.success(
                                    ProductsPage(
                                        items =
                                            listOf(
                                                ProductSummary(
                                                    id = 1,
                                                    title = "Fixed",
                                                    brand = null,
                                                    price = 1.0,
                                                    thumbnailUrl = null,
                                                    discountPercentage = null,
                                                    rating = null,
                                                    stock = null
                                                )
                                            ),
                                        total = 1,
                                        skip = 0,
                                        limit = 30
                                    )
                                )
                            }
                        }

                        override suspend fun getProductDetails(id: Int) = error("not used")
                    }
            )
        val vm =
            SearchViewModel(
                searchProducts = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )
        vm.onQueryChange("x")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SearchUiState.Error)

        val err = vm.uiState.value as SearchUiState.Error
        assertEquals("x", err.activeQuery)
        assertTrue(err.message.isNotBlank())

        vm.retryLastSearch()
        advanceUntilIdle()

        val ok = vm.uiState.value as SearchUiState.Success
        assertEquals("Fixed", ok.items.first().title)
        assertEquals(2, calls)
    }

    @Test
    fun `loadNextPage appends until end reached`() = runTest {
        val useCase =
            SearchProductsUseCase(
                repository =
                    object : co.uk.next.techtest.domain.repository.ProductsRepository {
                        override suspend fun getProductsPage(limit: Int, skip: Int) =
                            error("not used")

                        override suspend fun searchProducts(
                            query: String,
                            limit: Int,
                            skip: Int
                        ): Result<ProductsPage> =
                            Result.success(
                                ProductsPage(
                                    items =
                                        listOf(
                                            ProductSummary(
                                                id = skip + 1,
                                                title = "Item $skip",
                                                brand = null,
                                                price = 1.0,
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

                        override suspend fun getProductDetails(id: Int) = error("not used")
                    }
            )
        val vm =
            SearchViewModel(
                searchProducts = useCase,
                savedProductsRepository = FakeSavedProductsRepository()
            )
        vm.onQueryChange("q")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertEquals(1, (vm.uiState.value as SearchUiState.Success).items.size)

        vm.loadNextPage()
        advanceUntilIdle()

        val s = vm.uiState.value as SearchUiState.Success
        assertEquals(2, s.items.size)
        assertTrue(s.endReached)
    }
}
