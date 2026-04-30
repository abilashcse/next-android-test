package co.uk.next.techtest.data.remote

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
        return r
    }

    suspend fun getProductById(id: Int): ProductDetailsDto {
        var response: ProductDetailsDto? = null
        val ms = measureTimeMillis {
            response = httpClient.get("$baseUrl/products/$id").body()
        }
        val r = response!!
        return r
    }

    suspend fun searchProductsPage(
        query: String,
        limit: Int,
        skip: Int,
        select: String? = null
    ): ProductsListResponseDto {
        var response: ProductsListResponseDto? = null
        measureTimeMillis {
            response =
                httpClient.get("$baseUrl/products/search") {
                    parameter("q", query)
                    parameter("limit", limit)
                    parameter("skip", skip)
                    if (!select.isNullOrBlank()) {
                        parameter("select", select)
                    }
                }.body()
        }
        return response!!
    }
}

