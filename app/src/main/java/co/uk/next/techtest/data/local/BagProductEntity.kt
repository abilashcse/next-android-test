package co.uk.next.techtest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.uk.next.techtest.domain.model.ProductSummary

@Entity(tableName = "bag_products")
data class BagProductEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val brand: String?,
    val price: Double?,
    val thumbnailUrl: String?,
    val discountPercentage: Double?,
    val rating: Double?,
    val stock: Int?,
    val addedAtMillis: Long
) {
    fun toSummary(): ProductSummary =
        ProductSummary(
            id = id,
            title = title,
            brand = brand,
            price = price,
            thumbnailUrl = thumbnailUrl,
            discountPercentage = discountPercentage,
            rating = rating,
            stock = stock
        )

    companion object {
        fun from(summary: ProductSummary, addedAtMillis: Long): BagProductEntity =
            BagProductEntity(
                id = summary.id,
                title = summary.title,
                brand = summary.brand,
                price = summary.price,
                thumbnailUrl = summary.thumbnailUrl,
                discountPercentage = summary.discountPercentage,
                rating = summary.rating,
                stock = summary.stock,
                addedAtMillis = addedAtMillis
            )
    }
}
