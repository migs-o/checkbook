package com.example.checkbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.checkbook.data.CheckbookDatabase
import com.example.checkbook.data.TransactionRepository
import com.example.checkbook.ui.TransactionViewModel
import com.example.checkbook.ui.TransactionViewModelFactory
import com.example.checkbook.ui.screens.MainScreen
import com.example.checkbook.ui.theme.CheckbookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = CheckbookDatabase.getDatabase(applicationContext)
        val repository = TransactionRepository(database.transactionDao())
        
        setContent {
            CheckbookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: TransactionViewModel = viewModel(
                        factory = TransactionViewModelFactory(repository)
                    )
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
} 