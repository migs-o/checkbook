package com.example.checkbook.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    var name by mutableStateOf("")
        private set

    var iconName by mutableStateOf("")
        private set

    var iconColor by mutableStateOf(0)
        private set

    init {
        loadPaymentMethods()
    }

    fun updateName(newName: String) {
        name = newName
    }

    fun updateIconName(newIconName: String) {
        iconName = newIconName
    }

    fun updateIconColor(newIconColor: Int) {
        iconColor = newIconColor
    }

    fun addPaymentMethod() {
        if (name.isBlank() || iconName.isBlank()) {
            return
        }

        val paymentMethod = PaymentMethod(
            name = name,
            iconName = iconName,
            iconColor = iconColor
        )

        viewModelScope.launch {
            repository.insertPaymentMethod(paymentMethod)
            loadPaymentMethods()
            resetForm()
        }
    }

    fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            repository.updatePaymentMethod(paymentMethod)
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

    fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            repository.deletePaymentMethod(paymentMethod)
            loadPaymentMethods()
        }
    }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            _paymentMethods.value = repository.getAllPaymentMethods()
        }
    }

    private fun resetForm() {
        name = ""
        iconName = ""
        iconColor = 0
    }
} 