package com.example.checkbook.data

import com.example.checkbook.data.entity.Transaction
import kotlinx.coroutines.flow.Flow
import com.example.checkbook.data.entity.TransactionType
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getCurrentBalance(): Flow<Double> = transactionDao.getAllTransactions().map { transactions ->
        transactions.fold(0.0) { acc, transaction ->
            acc + when (transaction.type) {
                TransactionType.INCOME -> transaction.amount
                TransactionType.EXPENSE -> -transaction.amount
                else -> 0.0
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