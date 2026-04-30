package co.uk.next.techtest.core.ui.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.uk.next.techtest.R
import co.uk.next.techtest.core.navigation.Routes
import co.uk.next.techtest.core.navigation.TechTestNavHost
import co.uk.next.techtest.core.network.NetworkMonitor
import co.uk.next.techtest.core.ui.testing.onSurfaceArgb
import co.uk.next.techtest.core.ui.testing.surfaceArgb
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    viewModel: AppShellViewModel = koinViewModel(),
    networkMonitor: NetworkMonitor = koinInject()
) {
    val navController = rememberNavController()
    val selected by viewModel.selectedDestination.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val shouldShowChrome = currentRoute != Routes.ProductDetails
    val onSurfaceArgbValue = MaterialTheme.colorScheme.onSurface.toArgb()
    val surfaceArgbValue = MaterialTheme.colorScheme.surface.toArgb()

    val isOnline by networkMonitor.isOnline.collectAsState()

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("app_root")
                .semantics {
                    onSurfaceArgb = onSurfaceArgbValue
                    surfaceArgb = surfaceArgbValue
                }
    ) {
        if (!isOnline) {
            OfflineFullScreen(onRetry = { networkMonitor.refresh() })
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    if (shouldShowChrome) {
                        CenterAlignedTopAppBar(
                            modifier = Modifier.testTag("top_app_bar"),
                            title = {
                                Image(
                                    painter = painterResource(id = R.drawable.next_tech_test_wordmark),
                                    contentDescription = "Next Tech Test",
                                    modifier =
                                        Modifier
                                            .height(30.dp)
                                            .testTag("top_app_bar_logo"),
                                    contentScale = ContentScale.Fit
                                )
                            },
                            actions = {
                                IconButton(onClick = { /* cart placeholder */ }) {
                                    Icon(
                                        imageVector = Icons.Outlined.ShoppingCart,
                                        contentDescription = "Cart"
                                    )
                                }
                            }
                        )
                    }
                },
                bottomBar = {
                    if (shouldShowChrome) {
                        NavigationBar {
                            BottomNavDestination.entries.forEach { destination ->
                                NavigationBarItem(
                                    selected = selected == destination,
                                    onClick = {
                                        viewModel.onDestinationSelected(destination)
                                        navController.navigate(destination.route) {
                                            launchSingleTop = true
                                            restoreState = true
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.label
                                        )
                                    },
                                    label = { Text(destination.label) }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                TechTestNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
