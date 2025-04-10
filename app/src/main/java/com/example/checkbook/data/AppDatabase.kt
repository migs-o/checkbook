package com.example.checkbook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.checkbook.data.converter.DateConverter
import com.example.checkbook.data.dao.PaymentMethodDao
import com.example.checkbook.data.dao.TransactionDao
import com.example.checkbook.data.entity.PaymentMethod
import com.example.checkbook.data.entity.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transaction::class, PaymentMethod::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun paymentMethodDao(): PaymentMethodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "checkbook_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                // Pre-populate with a default payment method
                                val paymentMethodDao = database.paymentMethodDao()
                                val defaultPaymentMethod = PaymentMethod(
                                    name = "Cash",
                                    iconName = "money",
                                    iconColor = 0xFF4CAF50.toInt(),
                                    isActive = true
                                )
                                paymentMethodDao.insertPaymentMethod(defaultPaymentMethod)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 