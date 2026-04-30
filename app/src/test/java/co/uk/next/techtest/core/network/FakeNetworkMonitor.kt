package co.uk.next.techtest.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeNetworkMonitor(
    initialOnline: Boolean = true
) : NetworkMonitor {

    private val _isOnline = MutableStateFlow(initialOnline)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    override fun refresh() {}

    fun setOnline(value: Boolean) {
        _isOnline.value = value
    }
}
