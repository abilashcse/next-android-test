package co.uk.next.techtest.domain.model

data class ProductDetails(
    val id: Int,
    val title: String,
    val description: String?,
    val price: Double?,
    val discountPercentage: Double?,
    val rating: Double?,
    val stock: Int?,
    val brand: String?,
    val category: String?,
    val thumbnailUrl: String?,
    val imageUrls: List<String>,
    val reviews: List<Review>,
    val returnPolicy: String?,
    val shippingInformation: String?,
    val availabilityStatus: String?,
    val minimumOrderQuantity: Int?,
    val meta: Meta?,
    val tags: List<String>,
    val weight: Double?,
    val warrantyInformation: String?,
    val dimensions: Dimensions?
)

data class Review(
    val rating: Int?,
    val comment: String?,
    val date: String?,
    val reviewerName: String?,
    val reviewerEmail: String?
)

data class Meta(
    val createdAt: String?,
    val updatedAt: String?,
    val barcode: String?,
    val qrCode: String?
)

data class Dimensions(
    val width: Double?,
    val height: Double?,
    val depth: Double?
)

fun ProductDetails.toProductSummary(): ProductSummary =
    ProductSummary(
        id = id,
        title = title,
        brand = brand,
        price = price,
        thumbnailUrl = thumbnailUrl?.takeIf { it.isNotBlank() } ?: imageUrls.firstOrNull { it.isNotBlank() },
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock
    )
