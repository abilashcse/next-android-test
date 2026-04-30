package co.uk.next.techtest.data.repository

import co.uk.next.techtest.data.remote.DummyJsonApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductsRepositoryImplTest {

    @Test
    fun `getProductsPage maps dto to domain and preserves pagination`() = runTest {
        val engine =
            MockEngine { request: HttpRequestData ->
                val url: Url = request.url
                assertEquals("/products", url.encodedPath)
                // Make sure repository uses select for PLP.
                assertTrue(url.parameters["select"]?.contains("thumbnail") == true)
                respond(
                    content =
                        """
                        {
                          "products": [
                            {
                              "id": 1,
                              "title": null,
                              "price": 10.0,
                              "brand": "Brand",
                              "thumbnail": "https://example.com/t.png",
                              "discountPercentage": 12.5,
                              "rating": 4.8,
                              "stock": 9
                            }
                          ],
                          "total": 100,
                          "skip": 0,
                          "limit": 30
                        }
                        """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                )
            }

        val client =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

        val api = DummyJsonApiClient(httpClient = client, baseUrl = "https://test")
        val repo = ProductsRepositoryImpl(apiClient = api)

        val result = repo.getProductsPage(limit = 30, skip = 0).getOrThrow()
        assertEquals(100, result.total)
        assertEquals(0, result.skip)
        assertEquals(30, result.limit)
        assertEquals(1, result.items.size)
        // title maps via orEmpty()
        assertEquals("", result.items.first().title)
        assertEquals("Brand", result.items.first().brand)
        assertEquals(10.0, result.items.first().price ?: 0.0, 0.0)
    }

    @Test
    fun `getProductDetails maps nested fields safely`() = runTest {
        val engine =
            MockEngine { request ->
                assertTrue(request.url.encodedPath.startsWith("/products/"))
                respond(
                    content =
                        """
                        {
                          "id": 7,
                          "title": "T",
                          "description": "D",
                          "price": 99.0,
                          "discountPercentage": 0.0,
                          "rating": 4.0,
                          "stock": 1,
                          "brand": "B",
                          "category": "C",
                          "thumbnail": null,
                          "images": ["https://example.com/1.png"],
                          "reviews": [
                            {
                              "rating": 5,
                              "comment": "Nice",
                              "date": "2024-01-01T00:00:00Z",
                              "reviewerName": "X",
                              "reviewerEmail": "x@example.com"
                            }
                          ],
                          "returnPolicy": "30 days",
                          "shippingInformation": "Ships",
                          "availabilityStatus": "In Stock",
                          "minimumOrderQuantity": 1,
                          "meta": { "createdAt": "now", "updatedAt": "now", "barcode": "b", "qrCode": "q" },
                          "tags": ["t1"],
                          "weight": 1.2,
                          "warrantyInformation": "1 year",
                          "dimensions": { "width": 1.0, "height": 2.0, "depth": 3.0 }
                        }
                        """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                )
            }

        val client =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

        val api = DummyJsonApiClient(httpClient = client, baseUrl = "https://test")
        val repo = ProductsRepositoryImpl(apiClient = api)

        val product = repo.getProductDetails(id = 7).getOrThrow()
        assertEquals(7, product.id)
        assertEquals("T", product.title)
        assertEquals(1, product.reviews.size)
        assertEquals(5, product.reviews.first().rating)
        assertEquals("now", product.meta?.createdAt)
        assertEquals(1.0, product.dimensions?.width ?: 0.0, 0.0)
    }

    @Test
    fun `searchProducts calls search endpoint with q and select`() = runTest {
        val engine =
            MockEngine { request: HttpRequestData ->
                val url: Url = request.url
                assertEquals("/products/search", url.encodedPath)
                assertEquals("phone", url.parameters["q"])
                assertTrue(url.parameters["select"]?.contains("thumbnail") == true)
                respond(
                    content =
                        """
                        {
                          "products": [
                            {
                              "id": 2,
                              "title": "Phone",
                              "price": 99.0,
                              "brand": "B",
                              "thumbnail": "https://example.com/p.png",
                              "discountPercentage": 0.0,
                              "rating": 4.0,
                              "stock": 3
                            }
                          ],
                          "total": 1,
                          "skip": 0,
                          "limit": 30
                        }
                        """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                )
            }

        val client =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

        val api = DummyJsonApiClient(httpClient = client, baseUrl = "https://test")
        val repo = ProductsRepositoryImpl(apiClient = api)

        val result = repo.searchProducts(query = "phone", limit = 30, skip = 0).getOrThrow()
        assertEquals(1, result.total)
        assertEquals(1, result.items.size)
        assertEquals("Phone", result.items.first().title)
    }
}

