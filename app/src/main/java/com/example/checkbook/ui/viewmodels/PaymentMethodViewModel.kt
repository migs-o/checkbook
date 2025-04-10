package com.example.checkbook.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkbook.data.entity.PaymentMethod
import com.example.checkbook.data.repository.PaymentMethodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    private val repository: PaymentMethodRepository
) : ViewModel() {

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    init {
        loadPaymentMethods()
    }

    fun loadPaymentMethods() {
        viewModelScope.launch {
            repository.getAllPaymentMethods().let { methods ->
                _paymentMethods.value = methods
            }
        }
    }

    fun addPaymentMethod(name: String, iconName: String, iconColor: Int) {
        viewModelScope.launch {
            val paymentMethod = PaymentMethod(
                name = name,
                iconName = iconName,
                iconColor = iconColor,
                isActive = true
            )
            repository.insertPaymentMethod(paymentMethod)
            loadPaymentMethods()
        }
    }

    fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            repository.updatePaymentMethod(paymentMethod)
            loadPaymentMethods()
        }
    }

    fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            repository.deletePaymentMethod(paymentMethod)
            loadPaymentMethods()
        }
    }

    fun togglePaymentMethodActive(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            val updatedPaymentMethod = paymentMethod.copy(isActive = !paymentMethod.isActive)
            repository.updatePaymentMethod(updatedPaymentMethod)
            loadPaymentMethods()
        }
    }
} 