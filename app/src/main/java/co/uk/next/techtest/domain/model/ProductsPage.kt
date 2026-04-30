package co.uk.next.techtest.domain.model

data class ProductsPage(
    val items: List<ProductSummary>,
    val total: Int,
    val skip: Int,
    val limit: Int
)

