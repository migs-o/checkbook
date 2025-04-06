package com.example.checkbook.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.checkbook.data.Transaction
import com.example.checkbook.data.TransactionRepository
import com.example.checkbook.data.PaymentMethod
import com.example.checkbook.data.PaymentMethodRepository
import com.example.checkbook.data.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

sealed class TransactionUiState {
    object Loading : TransactionUiState()
    object Success : TransactionUiState()
    data class Error(val message: String) : TransactionUiState()
}

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {
    private val _transactionsState = MutableStateFlow<UiState<List<Transaction>>>(UiState.Loading)
    val transactionsState: StateFlow<UiState<List<Transaction>>> = _transactionsState

    private val _balanceState = MutableStateFlow<UiState<Double>>(UiState.Loading)
    val balanceState: StateFlow<UiState<Double>> = _balanceState

    private val _paymentMethodsState = MutableStateFlow<UiState<List<PaymentMethod>>>(UiState.Loading)
    val paymentMethodsState: StateFlow<UiState<List<PaymentMethod>>> = _paymentMethodsState

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

    init {
        viewModelScope.launch {
            try {
                repository.allTransactions.collect { transactions ->
                    _transactionsState.value = UiState.Success(transactions)
                }
            } catch (e: Exception) {
                _transactionsState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }

        viewModelScope.launch {
            try {
                repository.getCurrentBalance().collect { balance ->
                    _balanceState.value = UiState.Success(balance)
                }
            } catch (e: Exception) {
                _balanceState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }

        viewModelScope.launch {
            try {
                paymentMethodRepository.allActivePaymentMethods.collect { paymentMethods ->
                    _paymentMethodsState.value = UiState.Success(paymentMethods)
                }
            } catch (e: Exception) {
                _paymentMethodsState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

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

    fun update(transaction: Transaction) {
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

    fun delete(transaction: Transaction) {
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

class TransactionViewModelFactory(
    private val repository: TransactionRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository, paymentMethodRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 