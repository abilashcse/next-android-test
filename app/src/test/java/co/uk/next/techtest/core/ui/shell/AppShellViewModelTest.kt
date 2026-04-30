package co.uk.next.techtest.core.ui.shell

import org.junit.Assert.assertEquals
import org.junit.Test

class AppShellViewModelTest {

    @Test
    fun `default destination is Home`() {
        val vm = AppShellViewModel()
        assertEquals(BottomNavDestination.Home, vm.selectedDestination.value)
    }

    @Test
    fun `onDestinationSelected updates destination`() {
        val vm = AppShellViewModel()
        vm.onDestinationSelected(BottomNavDestination.Bag)
        assertEquals(BottomNavDestination.Bag, vm.selectedDestination.value)
    }
}

