package co.uk.next.techtest.domain.repository

import co.uk.next.techtest.domain.model.ProductDetails
import co.uk.next.techtest.domain.model.ProductsPage
import co.uk.next.techtest.domain.model.ProductSummary

interface ProductsRepository {
    suspend fun getProductsPage(limit: Int, skip: Int): Result<ProductsPage>
    suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<ProductsPage>
    suspend fun getProductDetails(id: Int): Result<ProductDetails>
}

