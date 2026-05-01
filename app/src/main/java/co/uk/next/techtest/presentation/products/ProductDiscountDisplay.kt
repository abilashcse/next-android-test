package co.uk.next.techtest.presentation.products

import co.uk.next.techtest.domain.model.ProductSummary
import java.util.Locale
import kotlin.math.abs

/** Minimum discount % to show the red "SALE" chip (vs orange numeric % badge). */
const val MAJOR_SALE_MIN_PERCENT: Double = 50.0

fun discountPercentValue(discountPercentage: Double?): Double =
    discountPercentage ?: 0.0

fun ProductSummary.discountPercentValue(): Double =
    discountPercentValue(discountPercentage)

fun ProductSummary.isMajorSale(): Boolean =
    discountPercentValue() >= MAJOR_SALE_MIN_PERCENT

fun ProductSummary.hasDiscountPricing(): Boolean =
    discountPercentValue() > 0.0

fun ProductSummary.showsMinorDiscountBadge(): Boolean =
    hasDiscountPricing() && !isMajorSale()

fun isMajorSaleDiscount(discountPercentage: Double?): Boolean =
    discountPercentValue(discountPercentage) >= MAJOR_SALE_MIN_PERCENT

fun hasDiscountPricing(discountPercentage: Double?): Boolean =
    discountPercentValue(discountPercentage) > 0.0

fun showsMinorDiscountBadge(discountPercentage: Double?): Boolean =
    hasDiscountPricing(discountPercentage) && !isMajorSaleDiscount(discountPercentage)

/**
 * Major-sale rows (discount >= [MAJOR_SALE_MIN_PERCENT]) first, then all others.
 * Preserves relative order within each bucket (stable partition). Safe to run after each
 * paginated merge; does not change item count or API `skip` semantics.
 */
fun List<ProductSummary>.prioritizeMajorSales(): List<ProductSummary> {
    val major = filter { it.isMajorSale() }
    val rest = filter { !it.isMajorSale() }
    return major + rest
}

/** Label for the orange chip: e.g. `12% OFF`, `12.5% OFF` (discount below major-sale threshold). */
fun formatMinorDiscountPercent(percent: Double): String {
    val p = percent.coerceAtLeast(0.0).coerceAtMost(99.9)
    val pct =
        if (abs(p - p.toInt()) < 1e-6) {
            "${p.toInt()}%"
        } else {
            String.format(Locale.UK, "%.1f%%", p)
        }
    return "$pct OFF"
}
