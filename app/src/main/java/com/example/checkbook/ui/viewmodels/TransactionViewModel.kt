package com.example.checkbook.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkbook.data.entity.Transaction
import com.example.checkbook.data.entity.TransactionType
import com.example.checkbook.data.repository.TransactionRepository
import com.example.checkbook.ui.TransactionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _currentBalance = MutableStateFlow(0.0)
    val currentBalance: StateFlow<Double> = _currentBalance.asStateFlow()

    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Success)
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            try {
                _uiState.value = TransactionUiState.Loading
                repository.getAllTransactions().collect { transactions ->
                    _transactions.value = transactions
                    updateBalance(transactions)
                    _uiState.value = TransactionUiState.Success
                }
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Failed to load transactions")
            }
        }
    }

    private fun updateBalance(transactions: List<Transaction>) {
        _currentBalance.value = transactions.fold(0.0) { balance, transaction ->
            when (transaction.type) {
                TransactionType.INCOME -> balance + transaction.amount
                TransactionType.EXPENSE -> balance - transaction.amount
            }
        }
    }

    fun addTransaction(description: String, amount: Double, type: TransactionType, paymentMethodId: Long?) {
        viewModelScope.launch {
            try {
                _uiState.value = TransactionUiState.Loading
                val transaction = Transaction(
                    description = description,
                    amount = amount,
                    type = type,
                    paymentMethodId = paymentMethodId,
                    date = LocalDate.now()
                )
                repository.insertTransaction(transaction)
                loadTransactions()
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Failed to add transaction")
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _uiState.value = TransactionUiState.Loading
                repository.updateTransaction(transaction)
                loadTransactions()
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Failed to update transaction")
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _uiState.value = TransactionUiState.Loading
                repository.deleteTransaction(transaction)
                loadTransactions()
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Failed to delete transaction")
            }
        }
    }

    fun clearError() {
        _uiState.value = TransactionUiState.Success
    }
} 