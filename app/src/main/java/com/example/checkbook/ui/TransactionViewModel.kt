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
    val transactions = repository.allTransactions
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
        checkNumber: String? = null,
        paymentMethodId: Long = 0L
    ) {
        viewModelScope.launch {
            try {
              _uiState.value = TransactionUiState.Loading
                val transaction = Transaction(
                    date = Date(),
                    amount = amount,
                    description = description,
                    type = type,
                    checkNumber = checkNumber,
                    balance = 0.0, // The repository will calculate the correct balance
                    paymentMethodId = paymentMethodId
                )
                repository.insertTransaction(transaction)
                _uiState.value = TransactionUiState.Success
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _uiState.value = TransactionUiState.Loading
                repository.updateTransaction(transaction)
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