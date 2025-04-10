package com.example.checkbook.data.repository

import com.example.checkbook.data.dao.PaymentMethodDao
import com.example.checkbook.data.entity.PaymentMethod
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentMethodRepository @Inject constructor(
    private val paymentMethodDao: PaymentMethodDao
) {
    suspend fun getAllPaymentMethods(): List<PaymentMethod> {
        return paymentMethodDao.getAllPaymentMethods()
    }

    suspend fun getActivePaymentMethods(): List<PaymentMethod> {
        return paymentMethodDao.getActivePaymentMethods()
    }

    suspend fun insertPaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodDao.insertPaymentMethod(paymentMethod)
    }

    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodDao.updatePaymentMethod(paymentMethod)
    }

    suspend fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodDao.deletePaymentMethod(paymentMethod)
    }
} 