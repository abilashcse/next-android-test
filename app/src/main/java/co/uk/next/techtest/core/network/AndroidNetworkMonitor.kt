package co.uk.next.techtest.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks default network using [ConnectivityManager.registerDefaultNetworkCallback].
 * Treats the device as offline unless the active network has INTERNET and VALIDATED.
 */
class AndroidNetworkMonitor(
    context: Context
) : NetworkMonitor {

    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isOnline = MutableStateFlow(computeIsOnline())
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val callback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                postUpdate()
            }

            override fun onLost(network: Network) {
                postUpdate()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                postUpdate()
            }
        }

    init {
        connectivityManager.registerDefaultNetworkCallback(callback)
        postUpdate()
    }

    private fun postUpdate() {
        mainHandler.post { _isOnline.value = computeIsOnline() }
    }

    private fun computeIsOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override fun refresh() {
        postUpdate()
    }
}
