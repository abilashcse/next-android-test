package co.uk.next.techtest.data

import kotlinx.serialization.Serializable

@Serializable
data class ProductsResponse(
    val products: List<Product>
)

@Serializable
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val brand: String? = null,
    val category: String,
    val thumbnail: String,
    val images: List<String>,
    val reviews: List<Review>,
    val returnPolicy: String,
    val shippingInformation: String,
    val availabilityStatus: String,
    val minimumOrderQuantity: Int,
    val meta: Meta,
    val tags: List<String>,
    val weight: Double,
    val warrantyInformation: String,
    val dimensions: Dimensions
)

@Serializable
data class Review(
    val rating: Int,
    val comment: String,
    val date: String,
    val reviewerName: String,
    val reviewerEmail: String
)

@Serializable
data class Meta(
    val createdAt: String,
    val updatedAt: String,
    val barcode: String,
    val qrCode: String
)

@Serializable
data class Dimensions(
    val width: Double,
    val height: Double,
    val depth: Double
)
