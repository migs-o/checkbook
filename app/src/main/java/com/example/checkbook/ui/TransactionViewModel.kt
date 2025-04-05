package com.example.checkbook.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.checkbook.data.Transaction
import com.example.checkbook.data.TransactionRepository
import com.example.checkbook.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    val transactions = repository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentBalance = repository.getCurrentBalance()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Success)
    val uiState: StateFlow<TransactionUiState> = _uiState

    fun addTransaction(
        amount: Double,
        description: String,
        type: TransactionType,
        checkNumber: String? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = TransactionUiState.Loading
                val currentBalanceValue = currentBalance.value
                val newBalance = when (type) {
                    TransactionType.DEPOSIT -> currentBalanceValue + amount
                    TransactionType.WITHDRAWAL, TransactionType.CHECK -> currentBalanceValue - amount
                }
                
                val transaction = Transaction(
                    date = Date(),
                    amount = amount,
                    description = description,
                    type = type,
                    checkNumber = checkNumber,
                    balance = newBalance
                )
                repository.insertTransaction(transaction)
                _uiState.value = TransactionUiState.Success
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _uiState.value = TransactionUiState.Loading
                repository.deleteTransaction(transaction)
                _uiState.value = TransactionUiState.Success
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun clearError() {
        _uiState.value = TransactionUiState.Success
    }
}

class TransactionViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 