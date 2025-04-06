package com.example.checkbook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActivePaymentMethods(): Flow<List<PaymentMethod>>

    @Query("SELECT * FROM payment_methods ORDER BY name ASC")
    fun getAllPaymentMethods(): Flow<List<PaymentMethod>>

    @Insert
    suspend fun insert(paymentMethod: PaymentMethod)

    @Update
    suspend fun update(paymentMethod: PaymentMethod)

    @Delete
    suspend fun delete(paymentMethod: PaymentMethod)

    @Query("SELECT * FROM payment_methods WHERE id = :id")
    suspend fun getPaymentMethodById(id: Int): PaymentMethod?
} 