package co.uk.next.techtest.core.navigation

object Routes {
    const val Products = "products"
    const val ProductDetails = "product/{id}"

    fun productDetails(id: Int): String = "product/$id"
}

