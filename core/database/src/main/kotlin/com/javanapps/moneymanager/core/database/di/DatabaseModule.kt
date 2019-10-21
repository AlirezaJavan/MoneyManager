package com.javanapps.moneymanager.core.database.di

import android.content.Context
import androidx.room.Room
import com.javanapps.moneymanager.core.database.MIGRATION_1_2
import com.javanapps.moneymanager.core.database.MIGRATION_2_3
import com.javanapps.moneymanager.core.database.MoneyManagerDatabase
import com.javanapps.moneymanager.core.database.dao.BankSmsRuleDao
import com.javanapps.moneymanager.core.database.dao.CategoryDao
import com.javanapps.moneymanager.core.database.dao.TransactionDao
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
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): MoneyManagerDatabase =
        Room
            .databaseBuilder(
                context,
                MoneyManagerDatabase::class.java,
                "moneymanager-database",
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun provideTransactionDao(database: MoneyManagerDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCategoryDao(database: MoneyManagerDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideBankSmsRuleDao(database: MoneyManagerDatabase): BankSmsRuleDao = database.bankSmsRuleDao()
}
