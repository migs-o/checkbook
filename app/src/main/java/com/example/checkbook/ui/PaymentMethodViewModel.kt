package com.example.checkbook.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.checkbook.data.PaymentMethod
import com.example.checkbook.data.PaymentMethodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PaymentMethodViewModel(private val repository: PaymentMethodRepository) : ViewModel() {
    val allActivePaymentMethods: Flow<List<PaymentMethod>> = repository.allActivePaymentMethods
    val allPaymentMethods: Flow<List<PaymentMethod>> = repository.allPaymentMethods

    fun addPaymentMethod(name: String) {
        viewModelScope.launch {
            repository.insert(PaymentMethod(name = name))
        }
    }

    fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            repository.update(paymentMethod)
        }
    }

    fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            repository.delete(paymentMethod)
        }
    }

    fun togglePaymentMethodActive(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            repository.update(paymentMethod.copy(isActive = !paymentMethod.isActive))
        }
    }
}

class PaymentMethodViewModelFactory(private val repository: PaymentMethodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentMethodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentMethodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 