package com.example.checkbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_methods")
data class PaymentMethod(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val isActive: Boolean = true
) 