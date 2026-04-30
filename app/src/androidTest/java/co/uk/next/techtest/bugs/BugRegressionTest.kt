package co.uk.next.techtest.bugs

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.uk.next.techtest.MainActivity
import co.uk.next.techtest.core.ui.testing.TestSemantics
import android.os.SystemClock
import kotlin.math.pow
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BugRegressionTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    /**
     * Bug 1 (Crash on app load): launching MainActivity should not crash, and PLP should render.
     */
    @Test
    fun bug1_appLaunches_and_plpRenders() {
        waitUntil(timeoutMs = 5_000) {
            rule.onAllNodes(hasTestTag("plp_grid")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("plp_grid").assertIsDisplayed()
    }

    /**
     * Bug 2 (below system bars): first PLP item should be laid out below the top app bar.
     */
    @Test
    fun bug2_plpContentIsNotUnderSystemBars() {
        waitUntil(timeoutMs = 5_000) {
            rule.onAllNodes(hasTestTag("product_tile_1")).fetchSemanticsNodes().isNotEmpty()
        }

        val appBarBounds = rule.onNodeWithTag("top_app_bar").fetchSemanticsNode().boundsInRoot
        val tileBounds = rule.onNodeWithTag("product_tile_1").fetchSemanticsNode().boundsInRoot

        // Tile content top should be below the app bar bottom (with some tolerance).
        assert(tileBounds.top >= appBarBounds.bottom - 1f)
    }

    /**
     * Bug 3 (magenta text): ensure theme onSurface is not hard-coded to magenta.
     */
    @Test
    fun bug3_textColorIsNotMagenta() {
        val node = rule.onNodeWithTag("app_root").fetchSemanticsNode()
        if (!node.config.contains(TestSemantics.OnSurfaceArgbKey)) {
            fail("Missing semantics key: ${TestSemantics.OnSurfaceArgbKey.name}")
        }
        val onSurfaceArgb = node.config[TestSemantics.OnSurfaceArgbKey]
        assertNotEquals(Color.Magenta.toArgb(), onSurfaceArgb)
    }

    /**
     * Accessibility-oriented contrast sanity check: Material3 `onSurface` vs `surface` should not be visually "low contrast".
     * This is intentionally lighter than WCAG-AA for ALL text weights, but catches obvious theme mistakes.
     */
    @Test
    fun bug3_themeOnSurfaceHasReasonableContrastWithSurface() {
        val node = rule.onNodeWithTag("app_root").fetchSemanticsNode()
        if (!node.config.contains(TestSemantics.OnSurfaceArgbKey)) {
            fail("Missing semantics key: ${TestSemantics.OnSurfaceArgbKey.name}")
        }
        if (!node.config.contains(TestSemantics.SurfaceArgbKey)) {
            fail("Missing semantics key: ${TestSemantics.SurfaceArgbKey.name}")
        }

        val onSurfaceArgb = node.config[TestSemantics.OnSurfaceArgbKey]
        val surfaceArgb = node.config[TestSemantics.SurfaceArgbKey]

        val ratio = wcagContrastRatio(onSurfaceArgb, surfaceArgb)

        assertTrue(
            "Expected contrast ratio >= 3.5 between onSurface and surface, got $ratio (onSurface=0x%08x, surface=0x%08x)"
                .format(onSurfaceArgb, surfaceArgb),
            ratio >= 3.5f
        )
    }

    /**
     * Bug 4 (requests too long): loading should resolve quickly into a rendered PLP grid.
     * Uses a fake repository via the instrumentation runner (no network).
     */
    @Test
    fun bug4_plpLoadsQuickly() {
        // Either loading appears or we go straight to content.
        waitUntil(timeoutMs = 2_000) {
            val hasLoading = rule.onAllNodes(hasTestTag("plp_loading")).fetchSemanticsNodes().isNotEmpty()
            val hasGrid = rule.onAllNodes(hasTestTag("plp_grid")).fetchSemanticsNodes().isNotEmpty()
            hasLoading || hasGrid
        }

        waitUntil(timeoutMs = 2_000) {
            rule.onAllNodes(hasTestTag("plp_grid")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("plp_grid").assertIsDisplayed()
    }

    private fun waitUntil(timeoutMs: Long, condition: () -> Boolean) {
        val start = SystemClock.elapsedRealtime()
        while (SystemClock.elapsedRealtime() - start < timeoutMs) {
            rule.waitForIdle()
            if (condition()) return
            SystemClock.sleep(50)
        }
        fail("Condition not met within ${timeoutMs}ms")
    }

    private fun srgbChannelToLinear(c: Float): Float {
        return if (c <= 0.04045f) {
            (c / 12.92f)
        } else {
            ((c + 0.055f) / 1.055f).pow(2.4f)
        }
    }

    private fun relativeLuminance(argb: Int): Float {
        val r = android.graphics.Color.red(argb) / 255f
        val g = android.graphics.Color.green(argb) / 255f
        val b = android.graphics.Color.blue(argb) / 255f
        val rl = srgbChannelToLinear(r)
        val gl = srgbChannelToLinear(g)
        val bl = srgbChannelToLinear(b)
        return 0.2126f * rl + 0.7152f * gl + 0.0722f * bl
    }

    private fun wcagContrastRatio(aArgb: Int, bArgb: Int): Float {
        val lumA = relativeLuminance(aArgb)
        val lumB = relativeLuminance(bArgb)
        val lighter = kotlin.math.max(lumA, lumB)
        val darker = kotlin.math.min(lumA, lumB)
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}

