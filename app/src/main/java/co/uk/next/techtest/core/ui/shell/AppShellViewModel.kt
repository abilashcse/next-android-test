package co.uk.next.techtest.core.ui.shell

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppShellViewModel : ViewModel() {
    private val _selectedDestination =
        MutableStateFlow<BottomNavDestination>(BottomNavDestination.Home)
    val selectedDestination: StateFlow<BottomNavDestination> = _selectedDestination.asStateFlow()

    fun onDestinationSelected(destination: BottomNavDestination) {
        _selectedDestination.value = destination
    }
}

