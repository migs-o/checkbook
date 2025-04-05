package com.example.checkbook

import android.app.Application
import com.example.checkbook.data.CheckbookDatabase
import com.example.checkbook.data.TransactionRepository
import com.example.checkbook.data.PaymentMethodRepository

class CheckbookApplication : Application() {
    private val database by lazy { CheckbookDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
    val paymentMethodRepository by lazy { PaymentMethodRepository(database.paymentMethodDao()) }
} 