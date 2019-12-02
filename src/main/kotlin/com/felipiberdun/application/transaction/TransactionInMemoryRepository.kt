package com.felipiberdun.application.transaction

import com.felipiberdun.domain.transaction.*
import io.reactivex.Maybe
import io.reactivex.Single
import java.util.*
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TransactionInMemoryRepository(@Inject @Named("TransactionDataSource") private val accountBalances: ConcurrentMap<UUID, CopyOnWriteArrayList<Transaction>>)
    : TransactionRepository {

    override fun createTransaction(transaction: Transaction): Single<Transaction> {
        return when (transaction) {
            is Deposit -> persistDeposit(transaction)
            is Transfer -> persistTransfer(transaction)
            is Withdraw -> persistWithdraw(transaction)
        }
    }

    override fun findByAccountId(accountId: UUID): Single<List<Transaction>> {
        return Single.just(accountBalances[accountId] ?: emptyList())
    }

    override fun findByAccountAndId(accountId: UUID, transactionId: UUID): Maybe<Transaction> {
        return findByAccountId(accountId)
                .flatMapMaybe { list -> list.find { it.id == transactionId }?.let { Maybe.just(it) } ?: Maybe.empty() }
    }

    private fun persistDeposit(deposit: Deposit): Single<Transaction> {
        return Single.defer {
            addTransactionToAccount(deposit.destination.id, deposit)

            Single.just(deposit)
        }
    }

    private fun persistTransfer(transfer: Transfer): Single<Transaction> {
        return Single.defer {
            addTransactionToAccount(transfer.origin.id, transfer)
            addTransactionToAccount(transfer.destination.id, transfer)

            Single.just(transfer)
        }

    }

    private fun persistWithdraw(withdraw: Withdraw): Single<Transaction> {
        return Single.defer {
            addTransactionToAccount(withdraw.origin.id, withdraw)

            Single.just(withdraw)
        }
    }

    private fun addTransactionToAccount(accountId: UUID, transaction: Transaction) {
        accountBalances.compute(accountId) { _, currentList ->
            val list = currentList ?: CopyOnWriteArrayList()
            list.add(transaction)
            list
        }
    }


}
