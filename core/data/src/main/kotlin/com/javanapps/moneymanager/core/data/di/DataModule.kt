package com.javanapps.moneymanager.core.data.di

import com.javanapps.moneymanager.core.data.legacy.DefaultLegacyDataReader
import com.javanapps.moneymanager.core.data.legacy.LegacyDataReader
import com.javanapps.moneymanager.core.data.repository.AuthRepository
import com.javanapps.moneymanager.core.data.repository.BankSmsRuleRepository
import com.javanapps.moneymanager.core.data.repository.CategoryRepository
import com.javanapps.moneymanager.core.data.repository.DefaultAuthRepository
import com.javanapps.moneymanager.core.data.repository.DefaultBankSmsRuleRepository
import com.javanapps.moneymanager.core.data.repository.DefaultCategoryRepository
import com.javanapps.moneymanager.core.data.repository.DefaultPreferencesRepository
import com.javanapps.moneymanager.core.data.repository.DefaultTransactionRepository
import com.javanapps.moneymanager.core.data.repository.PreferencesRepository
import com.javanapps.moneymanager.core.data.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun bindTransactionRepository(impl: DefaultTransactionRepository): TransactionRepository

    @Binds
    abstract fun bindCategoryRepository(impl: DefaultCategoryRepository): CategoryRepository

    @Binds
    abstract fun bindPreferencesRepository(impl: DefaultPreferencesRepository): PreferencesRepository

    @Binds
    abstract fun bindAuthRepository(impl: DefaultAuthRepository): AuthRepository

    @Binds
    abstract fun bindBankSmsRuleRepository(impl: DefaultBankSmsRuleRepository): BankSmsRuleRepository

    @Binds
    abstract fun bindLegacyDataReader(impl: DefaultLegacyDataReader): LegacyDataReader
}
