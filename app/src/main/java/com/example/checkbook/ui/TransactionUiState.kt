package com.example.checkbook.ui

sealed class TransactionUiState {
    data object Success : TransactionUiState()
    data object Loading : TransactionUiState()
    data class Error(val message: String) : TransactionUiState()
} 