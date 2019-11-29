package com.felipiberdun

import com.felipiberdun.domain.accounts.Account
import com.felipiberdun.domain.transaction.Transaction
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Named
import javax.inject.Singleton

@Factory
class DependencyFactory {
    @Bean
    @Singleton
    @Named("TransactionDataSource")
    fun createTransactionDataSource(): ConcurrentMap<UUID, CopyOnWriteArrayList<Transaction>> = ConcurrentHashMap()

    @Bean
    @Singleton
    @Named("accountDataSource")
    fun createAccountDataSource(): ConcurrentMap<UUID, Account> = ConcurrentHashMap()
}