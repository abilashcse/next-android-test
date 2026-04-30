package co.uk.next.techtest.core.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeNetworkMonitorTest {

    @Test
    fun `setOnline updates state`() = runTest {
        val monitor = FakeNetworkMonitor(initialOnline = true)
        assertTrue(monitor.isOnline.first())

        monitor.setOnline(false)
        assertFalse(monitor.isOnline.first())

        monitor.setOnline(true)
        assertTrue(monitor.isOnline.first())
    }
}
