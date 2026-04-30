package co.uk.next.techtest.data.remote

import android.util.Log
import co.uk.next.techtest.data.dto.ProductDetailsDto
import co.uk.next.techtest.data.dto.ProductsListResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.get
import kotlin.system.measureTimeMillis

class DummyJsonApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://dummyjson.com"
) {

    suspend fun getProductsPage(
        limit: Int,
        skip: Int,
        select: String? = null
    ): ProductsListResponseDto {
        Log.d("DummyJsonApiClient", "GET /products?limit=$limit&skip=$skip&select=${select ?: ""}")
        var response: ProductsListResponseDto? = null
        val ms = measureTimeMillis {
            response =
                httpClient.get("$baseUrl/products") {
                    parameter("limit", limit)
                    parameter("skip", skip)
                    if (!select.isNullOrBlank()) {
                        parameter("select", select)
                    }
                }.body()
        }
        val r = response!!
        Log.d(
            "DummyJsonApiClient",
            "GET /products -> ${r.products.size} items (total=${r.total}, skip=${r.skip}, limit=${r.limit}) in ${ms}ms"
        )
        return r
    }

    suspend fun getProductById(id: Int): ProductDetailsDto {
        Log.d("DummyJsonApiClient", "GET /products/$id")
        var response: ProductDetailsDto? = null
        val ms = measureTimeMillis {
            response = httpClient.get("$baseUrl/products/$id").body()
        }
        val r = response!!
        Log.d("DummyJsonApiClient", "GET /products/$id -> title='${r.title}' in ${ms}ms")
        return r
    }
}

