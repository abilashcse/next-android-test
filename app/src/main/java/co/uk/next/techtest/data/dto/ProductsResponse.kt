package co.uk.next.techtest.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductsListResponseDto(
    val products: List<ProductDto> = emptyList(),
    val total: Int? = null,
    val skip: Int? = null,
    val limit: Int? = null
)

@Serializable
data class ProductDto(
    val id: Int,
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val discountPercentage: Double? = null,
    val rating: Double? = null,
    val stock: Int? = null,
    val brand: String? = null,
    val category: String? = null,
    val thumbnail: String? = null,
    val images: List<String>? = null
)

/**
 * Full product model intended for `/products/{id}`.
 *
 * DummyJSON fields may vary by endpoint/version. Everything that can be absent is nullable
 * or has a safe default to avoid serialization crashes.
 */
@Serializable
data class ProductDetailsDto(
    val id: Int,
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val discountPercentage: Double? = null,
    val rating: Double? = null,
    val stock: Int? = null,
    val brand: String? = null,
    val category: String? = null,
    val thumbnail: String? = null,
    val images: List<String> = emptyList(),
    val reviews: List<ReviewDto> = emptyList(),
    val returnPolicy: String? = null,
    val shippingInformation: String? = null,
    val availabilityStatus: String? = null,
    val minimumOrderQuantity: Int? = null,
    val meta: MetaDto? = null,
    val tags: List<String> = emptyList(),
    val weight: Double? = null,
    val warrantyInformation: String? = null,
    val dimensions: DimensionsDto? = null
)

@Serializable
data class ReviewDto(
    val rating: Int? = null,
    val comment: String? = null,
    val date: String? = null,
    val reviewerName: String? = null,
    val reviewerEmail: String? = null
)

@Serializable
data class MetaDto(
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val barcode: String? = null,
    val qrCode: String? = null
)

@Serializable
data class DimensionsDto(
    val width: Double? = null,
    val height: Double? = null,
    val depth: Double? = null
)

