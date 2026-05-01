package co.uk.next.techtest.core.di

import android.util.Log
import androidx.room.Room
import co.uk.next.techtest.core.network.AndroidNetworkMonitor
import co.uk.next.techtest.core.network.NetworkMonitor
import co.uk.next.techtest.core.ui.shell.AppShellViewModel
import co.uk.next.techtest.data.local.TechTestDatabase
import co.uk.next.techtest.data.remote.DummyJsonApiClient
import co.uk.next.techtest.data.repository.ProductsRepositoryImpl
import co.uk.next.techtest.data.repository.SavedProductsRepositoryImpl
import co.uk.next.techtest.domain.repository.ProductsRepository
import co.uk.next.techtest.domain.repository.SavedProductsRepository
import co.uk.next.techtest.domain.usecase.GetProductDetailsUseCase
import co.uk.next.techtest.domain.usecase.GetProductsPageUseCase
import co.uk.next.techtest.domain.usecase.SearchProductsUseCase
import co.uk.next.techtest.presentation.productdetails.ProductDetailsViewModel
import co.uk.next.techtest.presentation.products.ProductsViewModel
import co.uk.next.techtest.presentation.saved.SavedViewModel
import co.uk.next.techtest.presentation.search.SearchViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlinx.serialization.json.Json

val appModule = module {
    single {
        HttpClient(CIO) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("Ktor", message)
                    }
                }
                level = LogLevel.INFO
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    single { DummyJsonApiClient(httpClient = get()) }

    single<ProductsRepository> { ProductsRepositoryImpl(get()) }

    single {
        Room.databaseBuilder(
            androidContext(),
            TechTestDatabase::class.java,
            "tech_test.db"
        ).build()
    }
    single { get<TechTestDatabase>().savedProductDao() }
    single<SavedProductsRepository> { SavedProductsRepositoryImpl(dao = get()) }

    factory { GetProductsPageUseCase(repository = get()) }
    factory { GetProductDetailsUseCase(repository = get()) }
    factory { SearchProductsUseCase(repository = get()) }

    single<NetworkMonitor> { AndroidNetworkMonitor(context = androidContext()) }

    viewModel { AppShellViewModel() }
    viewModel { ProductsViewModel(getProductsPage = get(), savedProductsRepository = get()) }
    viewModel { ProductDetailsViewModel(getProductDetails = get(), savedProductsRepository = get()) }
    viewModel { SearchViewModel(searchProducts = get(), savedProductsRepository = get()) }
    viewModel { SavedViewModel(savedProductsRepository = get()) }
}

