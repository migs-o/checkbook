package com.example.checkbook.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val amount: Double,
    val description: String,
    val type: TransactionType,
    val checkNumber: String?,
    val balance: Double
) 