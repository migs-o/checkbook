package com.example.checkbook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Transaction::class, PaymentMethod::class], version = 1)
@TypeConverters(Converters::class)
abstract class CheckbookDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun paymentMethodDao(): PaymentMethodDao

    companion object {
        @Volatile
        private var INSTANCE: CheckbookDatabase? = null

        fun getDatabase(context: Context): CheckbookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CheckbookDatabase::class.java,
                    "checkbook_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 