package com.example.checkbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.checkbook.data.CheckbookDatabase
import com.example.checkbook.data.PaymentMethodRepository
import com.example.checkbook.data.TransactionRepository
import com.example.checkbook.ui.PaymentMethodViewModel
import com.example.checkbook.ui.PaymentMethodViewModelFactory
import com.example.checkbook.ui.viewmodels.TransactionViewModel
import com.example.checkbook.ui.viewmodels.TransactionViewModelFactory
import com.example.checkbook.ui.screens.MainScreen
import com.example.checkbook.ui.screens.SettingsScreen
import com.example.checkbook.ui.theme.CheckbookTheme

class MainActivity : ComponentActivity() {
    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            (application as CheckbookApplication).repository,
            (application as CheckbookApplication).paymentMethodRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = CheckbookDatabase.getDatabase(this)
        val transactionRepository = TransactionRepository(database.transactionDao())
        val paymentMethodRepository = PaymentMethodRepository(database.paymentMethodDao())
        
        setContent {
            CheckbookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CheckbookApp(transactionRepository, paymentMethodRepository)
                }
            }
        }
    }
}

@Composable
fun CheckbookApp(
    transactionRepository: TransactionRepository,
    paymentMethodRepository: PaymentMethodRepository
) {
    val navController = rememberNavController()
    
    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(transactionRepository, paymentMethodRepository)
    )
    
    val paymentMethodViewModel: PaymentMethodViewModel = viewModel(
        factory = PaymentMethodViewModelFactory(paymentMethodRepository)
    )

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = transactionViewModel,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = paymentMethodViewModel
            )
        }
    }
} 