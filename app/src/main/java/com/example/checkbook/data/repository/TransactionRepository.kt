package com.example.checkbook.data.repository

import com.example.checkbook.data.dao.TransactionDao
import com.example.checkbook.data.entity.Transaction
import com.example.checkbook.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsInDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> =
        transactionDao.getTransactionsInDateRange(startDate, endDate)

    fun getBalance(): Flow<Double> = getAllTransactions().map { transactions ->
        transactions.fold(0.0) { balance, transaction ->
            when (transaction.type) {
                TransactionType.INCOME -> balance + transaction.amount
                TransactionType.EXPENSE -> balance - transaction.amount
            }
        }
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }
} 