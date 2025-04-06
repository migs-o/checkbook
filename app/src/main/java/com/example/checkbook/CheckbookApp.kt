package com.example.checkbook

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.checkbook.ui.screens.MainScreen
import com.example.checkbook.ui.screens.SettingsScreen
import com.example.checkbook.ui.theme.CheckbookTheme
import com.example.checkbook.ui.viewmodels.SettingsViewModel
import com.example.checkbook.ui.viewmodels.SettingsViewModelFactory
import com.example.checkbook.ui.viewmodels.TransactionViewModel
import com.example.checkbook.ui.PaymentMethodViewModel

@Composable
fun CheckbookApp(
    transactionViewModel: TransactionViewModel,
    paymentMethodViewModel: PaymentMethodViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )
    val themePreference by settingsViewModel.themePreference.collectAsState(initial = "system")
    
    val isDarkTheme = when (themePreference) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    CheckbookTheme(darkTheme = isDarkTheme) {
        Surface {
            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    MainScreen(
                        viewModel = transactionViewModel,
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        paymentMethodViewModel = paymentMethodViewModel
                    )
                }
            }
        }
    }
} 