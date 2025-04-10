package com.example.checkbook.di

import android.content.Context
import com.example.checkbook.data.AppDatabase
import com.example.checkbook.data.dao.PaymentMethodDao
import com.example.checkbook.data.dao.TransactionDao
import com.example.checkbook.data.repository.PaymentMethodRepository
import com.example.checkbook.data.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun providePaymentMethodDao(database: AppDatabase): PaymentMethodDao {
        return database.paymentMethodDao()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao
    ): TransactionRepository {
        return TransactionRepository(transactionDao)
    }

    @Provides
    @Singleton
    fun providePaymentMethodRepository(
        paymentMethodDao: PaymentMethodDao
    ): PaymentMethodRepository {
        return PaymentMethodRepository(paymentMethodDao)
    }
} 