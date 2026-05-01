package co.uk.next.techtest.presentation.products

import co.uk.next.techtest.domain.model.ProductSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductDiscountDisplayTest {

    private fun summary(id: Int, discount: Double?) =
        ProductSummary(
            id = id,
            title = "T$id",
            brand = null,
            price = 10.0,
            thumbnailUrl = null,
            discountPercentage = discount,
            rating = null,
            stock = null
        )

    @Test
    fun `prioritizeMajorSales puts fifty percent and above first preserving order within buckets`() {
        val input =
            listOf(
                summary(1, 10.0),
                summary(2, 50.0),
                summary(3, 60.0),
                summary(4, null),
                summary(5, 49.9),
                summary(6, 50.0)
            )
        val out = input.prioritizeMajorSales()

        assertEquals(listOf(2, 3, 6), out.take(3).map { it.id })
        assertEquals(listOf(1, 4, 5), out.drop(3).map { it.id })
    }

    @Test
    fun `isMajorSale true at fifty percent`() {
        assertTrue(summary(1, 50.0).isMajorSale())
    }

    @Test
    fun `formatMinorDiscountPercent shows percent OFF`() {
        assertEquals("12% OFF", formatMinorDiscountPercent(12.0))
        assertEquals("12.5% OFF", formatMinorDiscountPercent(12.5))
    }
}
