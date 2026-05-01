package co.uk.next.techtest

import android.app.Application
import co.uk.next.techtest.core.ui.shell.AppShellViewModel
import co.uk.next.techtest.domain.model.Dimensions
import co.uk.next.techtest.domain.model.Meta
import co.uk.next.techtest.domain.model.ProductDetails
import co.uk.next.techtest.domain.model.ProductsPage
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.model.Review
import co.uk.next.techtest.domain.repository.ProductsRepository
import co.uk.next.techtest.domain.repository.SavedProductsRepository
import co.uk.next.techtest.domain.usecase.GetProductDetailsUseCase
import co.uk.next.techtest.domain.usecase.GetProductsPageUseCase
import co.uk.next.techtest.domain.usecase.SearchProductsUseCase
import co.uk.next.techtest.core.network.FakeNetworkMonitor
import co.uk.next.techtest.core.network.NetworkMonitor
import co.uk.next.techtest.presentation.productdetails.ProductDetailsViewModel
import co.uk.next.techtest.presentation.products.ProductsViewModel
import co.uk.next.techtest.presentation.saved.SavedViewModel
import co.uk.next.techtest.presentation.search.SearchViewModel
import co.uk.next.techtest.testsupport.FakeSavedProductsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

class TestTechTestApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val fakeRepo =
            object : ProductsRepository {
                override suspend fun getProductsPage(limit: Int, skip: Int): Result<ProductsPage> =
                    Result.success(
                        ProductsPage(
                            items =
                                listOf(
                                    ProductSummary(
                                        id = 1,
                                        title = "Test Product",
                                        brand = "Brand",
                                        price = 10.0,
                                        thumbnailUrl = null,
                                        discountPercentage = 0.0,
                                        rating = 4.8,
                                        stock = 12
                                    )
                                ),
                            total = 1,
                            skip = skip,
                            limit = limit
                        )
                    )

                override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<ProductsPage> =
                    Result.success(
                        ProductsPage(
                            items = emptyList(),
                            total = 0,
                            skip = skip,
                            limit = limit
                        )
                    )

                override suspend fun getProductDetails(id: Int): Result<ProductDetails> =
                    Result.success(
                        ProductDetails(
                            id = id,
                            title = "Test Product",
                            description = "Desc",
                            price = 10.0,
                            discountPercentage = 0.0,
                            rating = 4.8,
                            stock = 12,
                            brand = "Brand",
                            category = "Cat",
                            thumbnailUrl = null,
                            imageUrls = emptyList(),
                            reviews =
                                listOf(
                                    Review(
                                        rating = 5,
                                        comment = "Great",
                                        date = "2024-01-01T00:00:00Z",
                                        reviewerName = "A",
                                        reviewerEmail = "a@example.com"
                                    )
                                ),
                            returnPolicy = "30 days",
                            shippingInformation = "Ships",
                            availabilityStatus = "In Stock",
                            minimumOrderQuantity = 1,
                            meta =
                                Meta(
                                    createdAt = "now",
                                    updatedAt = "now",
                                    barcode = "b",
                                    qrCode = "q"
                                ),
                            tags = emptyList(),
                            weight = 1.2,
                            warrantyInformation = "1 year",
                            dimensions = Dimensions(width = 1.0, height = 2.0, depth = 3.0)
                        )
                    )
            }

        val testModule =
            module {
                single<ProductsRepository> { fakeRepo }
                single<SavedProductsRepository> { FakeSavedProductsRepository() }
                single<NetworkMonitor> { FakeNetworkMonitor(initialOnline = true) }
                factory { GetProductsPageUseCase(repository = get()) }
                factory { GetProductDetailsUseCase(repository = get()) }
                factory { SearchProductsUseCase(repository = get()) }
                viewModel { AppShellViewModel() }
                viewModel { ProductsViewModel(getProductsPage = get(), savedProductsRepository = get()) }
                viewModel { ProductDetailsViewModel(getProductDetails = get(), savedProductsRepository = get()) }
                viewModel { SearchViewModel(searchProducts = get(), savedProductsRepository = get()) }
                viewModel { SavedViewModel(savedProductsRepository = get()) }
            }

        startKoin {
            androidContext(this@TestTechTestApp)
            modules(testModule)
        }
    }
}

