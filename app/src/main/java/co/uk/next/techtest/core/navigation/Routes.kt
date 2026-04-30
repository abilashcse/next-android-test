package co.uk.next.techtest.core.navigation

object Routes {
    const val Products = "products"
    const val Search = "search"
    const val Saved = "saved"
    const val Bag = "bag"
    const val Account = "account"
    const val ProductDetails = "product/{id}"

    fun productDetails(id: Int): String = "product/$id"
}

