package com.example.checkbook.data

import kotlinx.coroutines.flow.Flow

class PaymentMethodRepository(private val paymentMethodDao: PaymentMethodDao) {
    val allActivePaymentMethods: Flow<List<PaymentMethod>> = paymentMethodDao.getAllActivePaymentMethods()
    val allPaymentMethods: Flow<List<PaymentMethod>> = paymentMethodDao.getAllPaymentMethods()

    suspend fun insert(paymentMethod: PaymentMethod) {
        paymentMethodDao.insert(paymentMethod)
    }

    suspend fun update(paymentMethod: PaymentMethod) {
        paymentMethodDao.update(paymentMethod)
    }

    suspend fun delete(paymentMethod: PaymentMethod) {
        paymentMethodDao.delete(paymentMethod)
    }

    suspend fun getPaymentMethodById(id: Int): PaymentMethod? {
        return paymentMethodDao.getPaymentMethodById(id)
    }
} 