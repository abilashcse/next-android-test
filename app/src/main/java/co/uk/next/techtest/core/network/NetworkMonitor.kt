package co.uk.next.techtest.core.network

import kotlinx.coroutines.flow.StateFlow

interface NetworkMonitor {
    val isOnline: StateFlow<Boolean>

    /** Re-evaluate connectivity (e.g. user tapped Retry on offline screen). */
    fun refresh()
}
