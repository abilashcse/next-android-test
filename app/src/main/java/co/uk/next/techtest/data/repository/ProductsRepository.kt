package co.uk.next.techtest.data.repository

import co.uk.next.techtest.data.dto.ProductDto
import co.uk.next.techtest.data.dto.ProductDetailsDto
import co.uk.next.techtest.data.dto.DimensionsDto
import co.uk.next.techtest.data.dto.MetaDto
import co.uk.next.techtest.data.dto.ReviewDto
import co.uk.next.techtest.data.remote.DummyJsonApiClient
import co.uk.next.techtest.domain.model.ProductDetails
import co.uk.next.techtest.domain.model.Dimensions
import co.uk.next.techtest.domain.model.Meta
import co.uk.next.techtest.domain.model.ProductsPage
import co.uk.next.techtest.domain.model.ProductSummary
import co.uk.next.techtest.domain.model.Review
import co.uk.next.techtest.domain.repository.ProductsRepository

class ProductsRepositoryImpl(
    private val apiClient: DummyJsonApiClient
) : ProductsRepository {

    override suspend fun getProductsPage(limit: Int, skip: Int): Result<ProductsPage> {
        return runCatching {
            val response =
                apiClient.getProductsPage(
                    limit = limit,
                    skip = skip,
                    select = PLP_FIELD_SELECT
                )

            ProductsPage(
                items = response.products.map { it.toSummary() },
                total = response.total ?: 0,
                skip = response.skip ?: skip,
                limit = response.limit ?: limit
            )
        }
    }

    override suspend fun searchProducts(query: String, limit: Int, skip: Int): Result<ProductsPage> {
        return runCatching {
            val response =
                apiClient.searchProductsPage(
                    query = query,
                    limit = limit,
                    skip = skip,
                    select = PLP_FIELD_SELECT
                )
            ProductsPage(
                items = response.products.map { it.toSummary() },
                total = response.total ?: 0,
                skip = response.skip ?: skip,
                limit = response.limit ?: limit
            )
        }
    }

    override suspend fun getProductDetails(id: Int): Result<ProductDetails> {
        return runCatching {
            apiClient.getProductById(id).toDetails()
        }
    }
}

private const val PLP_FIELD_SELECT =
    "id,title,price,brand,thumbnail,discountPercentage,rating,stock"

private fun ProductDto.toSummary(): ProductSummary =
    ProductSummary(
        id = id,
        title = title.orEmpty(),
        brand = brand,
        price = price,
        thumbnailUrl = thumbnail,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock
    )

private fun ProductDetailsDto.toDetails(): ProductDetails =
    ProductDetails(
        id = id,
        title = title.orEmpty(),
        description = description,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        brand = brand,
        category = category,
        thumbnailUrl = thumbnail,
        imageUrls = images,
        reviews = reviews.map { it.toDomain() },
        returnPolicy = returnPolicy,
        shippingInformation = shippingInformation,
        availabilityStatus = availabilityStatus,
        minimumOrderQuantity = minimumOrderQuantity,
        meta = meta?.toDomain(),
        tags = tags,
        weight = weight,
        warrantyInformation = warrantyInformation,
        dimensions = dimensions?.toDomain()
    )

private fun ReviewDto.toDomain(): Review =
    Review(
        rating = rating,
        comment = comment,
        date = date,
        reviewerName = reviewerName,
        reviewerEmail = reviewerEmail
    )

private fun MetaDto.toDomain(): Meta =
    Meta(
        createdAt = createdAt,
        updatedAt = updatedAt,
        barcode = barcode,
        qrCode = qrCode
    )

private fun DimensionsDto.toDomain(): Dimensions =
    Dimensions(
        width = width,
        height = height,
        depth = depth
    )

