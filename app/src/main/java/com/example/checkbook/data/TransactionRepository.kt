package com.example.checkbook.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: TransactionDao) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getCurrentBalance(): Flow<Double> = transactionDao.getAllTransactions()
        .map { transactions -> 
            transactions.lastOrNull()?.balance ?: 0.0 
        }

    suspend fun getTransaction(id: Long): Transaction? = transactionDao.getTransaction(id)

    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)
} 