package co.uk.next.techtest.presentation.products.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import co.uk.next.techtest.domain.model.ProductSummary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.outlined.StarBorder
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductTile(
    product: ProductSummary,
    modifier: Modifier = Modifier,
    isFavourite: Boolean = false,
    onFavouriteClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.UK) }
    val priceText = remember(product.price) {
        product.price?.let(currencyFormatter::format) ?: ""
    }
    val discount = product.discountPercentage ?: 0.0
    val hasSale = discount > 0.0
    val originalPriceText = remember(product.price, discount) {
        val price = product.price ?: return@remember ""
        if (!hasSale || discount >= 100.0) return@remember ""
        val original = price / (1.0 - (discount / 100.0))
        currencyFormatter.format(original)
    }
    val brandText = remember(product.brand) {
        product.brand?.takeIf { it.isNotBlank() } ?: "Unknown Brand"
    }
    val thumbnailUrl = product.thumbnailUrl?.takeIf { it.isNotBlank() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium)
                ) {
                    if (thumbnailUrl == null) {
                        Text(
                            text = "Image Not Available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(8.dp)
                        )
                    } else {
                        AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = product.title,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop,
                            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                            error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }

                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Text(
                    text = brandText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .heightIn(min = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (priceText.isNotBlank()) priceText else "N/A",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (hasSale && originalPriceText.isNotBlank()) originalPriceText else " ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall.merge(
                            TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                    )
                }

                RatingRow(
                    rating = product.rating,
                    count = product.stock,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (hasSale) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color.Red)
                ) {
                    Text(
                        text = "SALE",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            IconButton(
                onClick = onFavouriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites"
                )
            }
        }
    }
}

@Composable
private fun RatingRow(
    rating: Double?,
    count: Int?,
    modifier: Modifier = Modifier
) {
    if (rating == null) return
    val clamped = rating.coerceIn(0.0, 5.0)
    val full = clamped.toInt()
    val hasHalf = (clamped - full) >= 0.5
    val starColor = androidx.compose.ui.graphics.Color(0xFFFFC107)

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            val icon =
                when {
                    i <= full -> Icons.Filled.Star
                    i == full + 1 && hasHalf -> Icons.AutoMirrored.Filled.StarHalf
                    else -> Icons.Outlined.StarBorder
                }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (icon == Icons.Outlined.StarBorder) starColor.copy(alpha = 0.35f) else starColor
            )
        }

        if (count != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "($count)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

