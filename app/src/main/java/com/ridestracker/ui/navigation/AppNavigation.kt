package com.ridestracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ridestracker.ui.screen.history.HistoryScreen
import com.ridestracker.ui.screen.history.RideDetailScreen
import com.ridestracker.ui.screen.settings.EmergencyContactScreen
import com.ridestracker.ui.screen.settings.MaintenanceScreen
import com.ridestracker.ui.screen.settings.FuelLogScreen
import com.ridestracker.ui.screen.settings.SettingsScreen
import com.ridestracker.ui.screen.stats.StatsScreen
import com.ridestracker.ui.screen.track.TrackScreen
import com.ridestracker.ui.screen.summary.RideSummaryScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Track : Screen("track", "Track", Icons.Default.DirectionsBike)
    object History : Screen("history", "History", Icons.Default.History)
    object Stats : Screen("stats", "Stats", Icons.Default.BarChart)
    object More : Screen("more", "More", Icons.Default.MoreHoriz)
}

val bottomNavItems = listOf(Screen.Track, Screen.History, Screen.Stats, Screen.More)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Track.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Track.route) {
                TrackScreen(onNavigateToSummary = { rideId ->
                    navController.navigate("summary/$rideId")
                })
            }
            composable(Screen.History.route) {
                HistoryScreen(onRideClick = { rideId ->
                    navController.navigate("ride_detail/$rideId")
                })
            }
            composable(Screen.Stats.route) { StatsScreen() }
            composable(Screen.More.route) {
                SettingsScreen(
                    onNavigateMaintenance = { navController.navigate("maintenance") },
                    onNavigateFuel = { navController.navigate("fuel") },
                    onNavigateEmergencyContact = { navController.navigate("emergency_contact") }
                )
            }
            composable(
                "summary/{rideId}",
                arguments = listOf(navArgument("rideId") { type = NavType.StringType })
            ) { backStack ->
                RideSummaryScreen(
                    rideId = backStack.arguments?.getString("rideId") ?: "",
                    onDone = { navController.navigate(Screen.History.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }}
                )
            }
            composable(
                "ride_detail/{rideId}",
                arguments = listOf(navArgument("rideId") { type = NavType.StringType })
            ) { backStack ->
                RideDetailScreen(
                    rideId = backStack.arguments?.getString("rideId") ?: "",
                    onBack = { navController.popBackStack() }
                )
            }
            composable("maintenance") {
                MaintenanceScreen(onBack = { navController.popBackStack() })
            }
            composable("fuel") {
                FuelLogScreen(onBack = { navController.popBackStack() })
            }
            composable("emergency_contact") {
                EmergencyContactScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
