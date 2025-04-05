package com.example.checkbook.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: TransactionDao) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getCurrentBalance(): Flow<Double> = transactionDao.getAllTransactions()
        .map { transactions -> 
            transactions.fold(0.0) { acc, transaction ->
                when (transaction.type) {
                    TransactionType.DEPOSIT -> acc + transaction.amount
                    TransactionType.WITHDRAWAL, TransactionType.CHECK -> acc - transaction.amount
                }
            }
        }

    suspend fun getTransaction(id: Long): Transaction? = transactionDao.getTransaction(id)

    suspend fun insertTransaction(transaction: Transaction) {
        // Calculate the new balance based on all previous transactions
        val currentTransactions = transactionDao.getAllTransactionsSync()
        val currentBalance = currentTransactions.fold(0.0) { acc, t ->
            when (t.type) {
                TransactionType.DEPOSIT -> acc + t.amount
                TransactionType.WITHDRAWAL, TransactionType.CHECK -> acc - t.amount
            }
        }
        
        // Update the balance for the new transaction
        val newBalance = when (transaction.type) {
            TransactionType.DEPOSIT -> currentBalance + transaction.amount
            TransactionType.WITHDRAWAL, TransactionType.CHECK -> currentBalance - transaction.amount
        }
        
        transactionDao.insertTransaction(transaction.copy(balance = newBalance))
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
        // After updating, recalculate balances for all transactions
        val allTransactions = transactionDao.getAllTransactionsSync()
        var runningBalance = 0.0
        allTransactions.forEach { t ->
            runningBalance += when (t.type) {
                TransactionType.DEPOSIT -> t.amount
                TransactionType.WITHDRAWAL, TransactionType.CHECK -> -t.amount
            }
            if (t.balance != runningBalance) {
                transactionDao.updateTransaction(t.copy(balance = runningBalance))
            }
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
        // After deleting, update balances of all subsequent transactions
        val remainingTransactions = transactionDao.getAllTransactionsSync()
        var runningBalance = 0.0
        remainingTransactions.forEach { t ->
            runningBalance = when (t.type) {
                TransactionType.DEPOSIT -> runningBalance + t.amount
                TransactionType.WITHDRAWAL, TransactionType.CHECK -> runningBalance - t.amount
            }
            transactionDao.updateTransaction(t.copy(balance = runningBalance))
        }
    }
} 