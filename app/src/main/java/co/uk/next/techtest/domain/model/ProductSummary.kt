package co.uk.next.techtest.domain.model

data class ProductSummary(
    val id: Int,
    val title: String,
    val brand: String?,
    val price: Double?,
    val thumbnailUrl: String?,
    val discountPercentage: Double?,
    val rating: Double?,
    val stock: Int?
)

