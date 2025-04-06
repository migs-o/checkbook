package com.example.checkbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.checkbook.data.CheckbookDatabase
import com.example.checkbook.data.PaymentMethodRepository
import com.example.checkbook.data.TransactionRepository
import com.example.checkbook.ui.PaymentMethodViewModel
import com.example.checkbook.ui.PaymentMethodViewModelFactory
import com.example.checkbook.ui.viewmodels.TransactionViewModel
import com.example.checkbook.ui.viewmodels.TransactionViewModelFactory
import com.example.checkbook.ui.theme.CheckbookTheme

class MainActivity : ComponentActivity() {
    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            (application as CheckbookApplication).repository,
            (application as CheckbookApplication).paymentMethodRepository
        )
    }
    
    private val paymentMethodViewModel: PaymentMethodViewModel by viewModels {
        PaymentMethodViewModelFactory(
            (application as CheckbookApplication).paymentMethodRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CheckbookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CheckbookApp(
                        transactionViewModel = transactionViewModel,
                        paymentMethodViewModel = paymentMethodViewModel
                    )
                }
            }
        }
    }
} 