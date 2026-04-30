package co.uk.next.techtest.core.di

import android.util.Log
import co.uk.next.techtest.data.remote.DummyJsonApiClient
import co.uk.next.techtest.data.repository.ProductsRepositoryImpl
import co.uk.next.techtest.domain.repository.ProductsRepository
import co.uk.next.techtest.domain.usecase.GetProductsPageUseCase
import co.uk.next.techtest.domain.usecase.GetProductDetailsUseCase
import co.uk.next.techtest.presentation.productdetails.ProductDetailsViewModel
import co.uk.next.techtest.presentation.products.ProductsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
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
    factory { GetProductsPageUseCase(repository = get()) }
    factory { GetProductDetailsUseCase(repository = get()) }

    viewModel { ProductsViewModel(getProductsPage = get()) }
    viewModel { ProductDetailsViewModel(getProductDetails = get()) }
}

