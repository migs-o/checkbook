package com.example.checkbook

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.checkbook.ui.screens.*
import com.example.checkbook.ui.viewmodels.PaymentMethodViewModel
import com.example.checkbook.ui.viewmodels.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckbookApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.entries.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                val transactionViewModel: TransactionViewModel = hiltViewModel()
                val paymentMethodViewModel: PaymentMethodViewModel = hiltViewModel()
                MainScreen(
                    navController = navController,
                    transactionViewModel = transactionViewModel,
                    paymentMethodViewModel = paymentMethodViewModel
                )
            }
            composable(Screen.Analytics.route) {
                val transactionViewModel: TransactionViewModel = hiltViewModel()
                AnalyticsScreen(transactionViewModel = transactionViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

sealed class Screen(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    object Home : Screen("home", Icons.Default.Home, "Home")
    object Analytics : Screen("analytics", Icons.Default.List, "Analytics")
    object Settings : Screen("settings", Icons.Default.Settings, "Settings")
    
    companion object {
        val entries = listOf(Home, Analytics, Settings)
    }
} 