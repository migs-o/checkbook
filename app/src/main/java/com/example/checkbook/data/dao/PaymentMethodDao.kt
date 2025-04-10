package com.example.checkbook.data.dao

import androidx.room.*
import com.example.checkbook.data.entity.PaymentMethod

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods ORDER BY name ASC")
    suspend fun getAllPaymentMethods(): List<PaymentMethod>

    @Query("SELECT * FROM payment_methods WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActivePaymentMethods(): List<PaymentMethod>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethod)

    @Update
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod)

    @Delete
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethod)
} 