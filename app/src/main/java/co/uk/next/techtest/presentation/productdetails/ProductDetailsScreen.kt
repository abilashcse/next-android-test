package co.uk.next.techtest.presentation.productdetails

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import co.uk.next.techtest.presentation.products.formatMinorDiscountPercent
import co.uk.next.techtest.presentation.products.hasDiscountPricing
import co.uk.next.techtest.presentation.products.isMajorSaleDiscount
import co.uk.next.techtest.presentation.products.showsMinorDiscountBadge
import org.koin.compose.viewmodel.koinViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.time.OffsetDateTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    productId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailsViewModel = koinViewModel()
) {
    LaunchedEffect(productId) {
        viewModel.load(productId)
    }

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ProductDetailsUiState.Loading -> {
            Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }

        is ProductDetailsUiState.Error -> {
            Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(onClick = { viewModel.load(productId) }) {
                    Text(text = "Retry")
                }
                Button(onClick = onBack) {
                    Text(text = "Back")
                }
            }
        }

        is ProductDetailsUiState.Success -> {
            val product = state.product
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.UK)
            val isSaved by viewModel.isSaved.collectAsState()
            val bringIntoViewRequester = remember { BringIntoViewRequester() }
            val scope = rememberCoroutineScope()
            val latestOnBack by rememberUpdatedState(onBack)

            val configuration = LocalConfiguration.current
            val density = LocalDensity.current
            val screenHeightPx = remember(configuration.screenHeightDp, density) {
                with(density) { configuration.screenHeightDp.dp.toPx() }
            }

            // 0f = fully offscreen (below). 1f = fully shown.
            val progress = remember(productId) { Animatable(0f) }
            val isDismissing = remember { mutableStateOf(false) }

            val discount = product.discountPercentage ?: 0.0
            val productHasDiscountPricing = hasDiscountPricing(product.discountPercentage)
            val majorSalePdp = isMajorSaleDiscount(product.discountPercentage)
            val minorSalePdp = showsMinorDiscountBadge(product.discountPercentage)
            val priceText = product.price?.let(currencyFormatter::format).orEmpty()
            val originalPriceText = remember(product.price, discount) {
                val price = product.price ?: return@remember ""
                if (!productHasDiscountPricing || discount >= 100.0) return@remember ""
                val original = price / (1.0 - (discount / 100.0))
                currencyFormatter.format(original)
            }

            val translationY = (1f - progress.value) * screenHeightPx
            val scrimAlpha = progress.value.coerceIn(0f, 1f)

            suspend fun dismissToPlp() {
                if (isDismissing.value) return
                isDismissing.value = true
                try {
                    progress.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 240)
                    )
                } finally {
                    latestOnBack()
                }
            }

            // Entry transition: slide up + fade in background.
            LaunchedEffect(productId) {
                isDismissing.value = false
                progress.snapTo(0f)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 280)
                )
            }

            // Predictive back: scrubbable slide-down + reveal PLP.
            PredictiveBackHandler(enabled = true) { backProgress: Flow<BackEventCompat> ->
                try {
                    backProgress.collect { event ->
                        progress.snapTo((1f - event.progress).coerceIn(0f, 1f))
                    }
                    // Gesture committed.
                    dismissToPlp()
                } catch (e: CancellationException) {
                    // Gesture cancelled.
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                    throw e
                }
            }

            val scrollState = rememberScrollState()
            val scrollAtTop by remember {
                derivedStateOf { scrollState.value == 0 }
            }

            Box(
                modifier = modifier
                    .fillMaxSize()
                    // Scrim drives the perceived PLP fade-away/fade-in underneath the PDP.
                    .background(Color.Black.copy(alpha = 0.22f * scrimAlpha))
            ) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            this.translationY = translationY
                        }
                        .pointerInput(scrollAtTop, screenHeightPx) {
                            // Swipe-down-to-dismiss (scrubbable), only when content is at top.
                            if (!scrollAtTop) return@pointerInput

                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    // dragAmount > 0 when dragging down.
                                    change.consume()
                                    val delta = dragAmount / screenHeightPx
                                    val newProgress = (progress.value - delta).coerceIn(0f, 1f)
                                    scope.launch {
                                        progress.snapTo(newProgress)
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        progress.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                        )
                                    }
                                },
                                onDragEnd = {
                                    scope.launch {
                                        if (progress.value < 0.6f) {
                                            dismissToPlp()
                                        } else {
                                            progress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                            )
                                        }
                                    }
                                }
                            )
                        },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(text = product.title, maxLines = 1) },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { dismissToPlp() } }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { /* share placeholder */ }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Share,
                                        contentDescription = "Share"
                                    )
                                }
                                IconButton(onClick = { viewModel.toggleSaved() }) {
                                    Icon(
                                        imageVector =
                                            if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription =
                                            if (isSaved) "Remove from saved" else "Save product"
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    WindowInsets
                                        .navigationBars
                                        .asPaddingValues()
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Button(
                                onClick = { /* CTA placeholder */ },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Add to Cart")
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                    Text(
                        text = "SKU: ${product.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProductImageCarousel(
                        title = product.title,
                        imageUrls = product.imageUrls.ifEmpty { product.thumbnailUrl?.let(::listOf).orEmpty() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price + sale formatting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = priceText.ifBlank { "N/A" },
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (productHasDiscountPricing && originalPriceText.isNotBlank()) {
                            Text(
                                text = originalPriceText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge.merge(
                                    TextStyle(textDecoration = TextDecoration.LineThrough)
                                ),
                                modifier = Modifier.padding(top = 10.dp)
                            )
                            when {
                                majorSalePdp -> {
                                    Box(
                                        modifier =
                                            Modifier
                                                .padding(top = 10.dp)
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
                                minorSalePdp -> {
                                    val minorOrange = Color(0xFFFF9800)
                                    Box(
                                        modifier =
                                            Modifier
                                                .padding(top = 10.dp)
                                                .clip(MaterialTheme.shapes.small)
                                                .background(minorOrange)
                                    ) {
                                        Text(
                                            text = formatMinorDiscountPercent(discount),
                                            color = Color.Black,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    product.rating?.let { rating ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier.clickable(
                                role = Role.Button,
                                onClick = { scope.launch { bringIntoViewRequester.bringIntoView() } }
                            )
                        ) {
                            RatingRow(
                                rating = rating,
                                count = product.reviews.size.takeIf { it > 0 } ?: product.stock
                            )
                        }
                    }

                    product.brand?.takeIf { it.isNotBlank() }?.let { brand ->
                        Text(
                            text = "Brand: $brand",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    product.category?.takeIf { it.isNotBlank() }?.let { category ->
                        Text(
                            text = "Category: $category",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    product.description?.takeIf { it.isNotBlank() }?.let { description ->
                        Text(text = "Description", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    product.warrantyInformation?.takeIf { it.isNotBlank() }?.let { warranty ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Warranty", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = warranty,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    product.returnPolicy?.takeIf { it.isNotBlank() }?.let { returnPolicy ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Return policy", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = returnPolicy,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    product.shippingInformation?.takeIf { it.isNotBlank() }?.let { shipping ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Support & shipping", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = shipping,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (product.reviews.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Column(
                            modifier = Modifier
                                .bringIntoViewRequester(bringIntoViewRequester)
                        ) {
                            Text(text = "Reviews", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Redline: show rating text then stars after the Reviews heading.
                            product.rating?.let { rating ->
                                ReviewsHeaderRating(
                                    rating = rating,
                                    count = product.reviews.size
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            product.reviews.takeLast(10).reversed().forEach { review ->
                                ReviewItem(
                                    name = review.reviewerName,
                                    rating = review.rating,
                                    comment = review.comment,
                                    date = review.date
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    // Bottom bar covers the primary action.
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        }
    }
}

}

@Composable
private fun ProductImageCarousel(
    title: String,
    imageUrls: List<String>
) {
    val pagerState = rememberPagerState(pageCount = { imageUrls.size.coerceAtLeast(1) })
    val isZoomed = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(MaterialTheme.shapes.large)
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !isZoomed.value,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val url = imageUrls.getOrNull(page)
            ZoomableImage(
                model = url,
                contentDescription = title,
                onZoomChanged = { zoomed -> isZoomed.value = zoomed }
            )
        }

        // Indicator inside the image area (bottom-center).
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val count = imageUrls.size.coerceAtLeast(1)
            repeat(count) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    model: String?,
    contentDescription: String?,
    onZoomChanged: (Boolean) -> Unit
) {
    var scale = remember { mutableStateOf(1f) }
    var offsetX = remember { mutableStateOf(0f) }
    var offsetY = remember { mutableStateOf(0f) }

    val zoomed = scale.value > 1.01f
    SideEffect { onZoomChanged(zoomed) }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationX = offsetX.value
                translationY = offsetY.value
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()

                        val isPinching = pressedCount >= 2
                        val isPanningWhileZoomed = pressedCount == 1 && scale.value > 1.01f

                        if (isPinching || isPanningWhileZoomed) {
                            if (isPinching) {
                                val newScale = (scale.value * zoomChange).coerceIn(1f, 4f)
                                scale.value = newScale
                            }

                            if (scale.value > 1.01f) {
                                offsetX.value += panChange.x
                                offsetY.value += panChange.y
                            } else {
                                offsetX.value = 0f
                                offsetY.value = 0f
                            }

                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentScale = ContentScale.Crop,
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
        error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun RatingRow(
    rating: Double,
    count: Int?
) {
    val clamped = rating.coerceIn(0.0, 5.0)
    val full = clamped.toInt()
    val hasHalf = (clamped - full) >= 0.5
    val starColor = Color(0xFFFFC107)

    Row(verticalAlignment = Alignment.CenterVertically) {
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
                text = "($count reviews)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReviewsHeaderRating(
    rating: Double,
    count: Int
) {
    val clamped = rating.coerceIn(0.0, 5.0)
    val starColor = Color(0xFFFFC107)
    val full = clamped.toInt()
    val hasHalf = (clamped - full) >= 0.5

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${"%.1f".format(clamped)} Stars",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(10.dp))
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
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReviewItem(
    name: String?,
    rating: Int?,
    comment: String?,
    date: String?
) {
    val starColor = Color(0xFFFFC107)
    val clamped = (rating ?: 0).coerceIn(0, 5)
    val formattedDate = date?.let(::formatReviewDate)
    val safeComment = comment?.takeIf { it.isNotBlank() } ?: "No review text"

    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        // Name
        Text(
            text = name?.takeIf { it.isNotBlank() } ?: "Anonymous",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        // User rating (gold stars)
        Row(
            modifier = Modifier.padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                val icon = if (i <= clamped) Icons.Filled.Star else Icons.Outlined.StarBorder
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (icon == Icons.Outlined.StarBorder) starColor.copy(alpha = 0.35f) else starColor
                )
            }
        }

        // Review in double quotes
        Text(
            text = "“$safeComment”",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Date: DD - Month - YYYY
        if (!formattedDate.isNullOrBlank()) {
            Text(
                text = formattedDate.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

private fun formatReviewDate(raw: String): String {
    val output = DateTimeFormatter.ofPattern("dd - MMMM - yyyy")
    // Try a couple of common formats; if parsing fails, return raw.
    return runCatching { OffsetDateTime.parse(raw).format(output) }
        .recoverCatching { LocalDateTime.parse(raw).atOffset(ZoneOffset.UTC).format(output) }
        .getOrElse { raw }
}

