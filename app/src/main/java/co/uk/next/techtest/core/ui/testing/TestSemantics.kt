package co.uk.next.techtest.core.ui.testing

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

object TestSemantics {
    val OnSurfaceArgbKey = SemanticsPropertyKey<Int>("onSurfaceArgb")
    val SurfaceArgbKey = SemanticsPropertyKey<Int>("surfaceArgb")
}

var SemanticsPropertyReceiver.onSurfaceArgb by TestSemantics.OnSurfaceArgbKey
var SemanticsPropertyReceiver.surfaceArgb by TestSemantics.SurfaceArgbKey

