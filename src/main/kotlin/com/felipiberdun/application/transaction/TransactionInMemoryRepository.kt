package com.felipiberdun.application.transaction

import com.felipiberdun.domain.transaction.*
import io.reactivex.Single
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Singleton

@Singleton
class TransactionInMemoryRepository : TransactionRepository {

    private val accountBalances: ConcurrentMap<UUID, CopyOnWriteArrayList<Transaction>> = ConcurrentHashMap()

    override fun createTransaction(transaction: Transaction): Single<Transaction> {
        return when (transaction) {
            is Deposit -> persistDeposit(transaction)
            is Transfer -> persistTransfer(transaction)
            is Withdraw -> persistWithdraw(transaction)
        }
    }

    override fun findByAccountId(accountId: UUID): Single<List<Transaction>> {
        return Single.defer {
            Single.just(accountBalances[accountId] ?: emptyList<Transaction>())
        }
    }

    private fun persistDeposit(deposit: Deposit): Single<Transaction> {
        return Single.defer {
            addTransactionToAccount(deposit.to.id, deposit)

            Single.just(deposit)
        }
    }

    private fun persistTransfer(transfer: Transfer): Single<Transaction> {
        return Single.defer {
            addTransactionToAccount(transfer.from.id, transfer)
            addTransactionToAccount(transfer.to.id, transfer)

            Single.just(transfer)
        }

    }

    private fun persistWithdraw(withdraw: Withdraw): Single<Transaction> {
        return Single.defer {
            addTransactionToAccount(withdraw.from.id, withdraw)

            Single.just(withdraw)
        }
    }

    private fun addTransactionToAccount(accountId: UUID, transaction: Transaction) {
        val compute = accountBalances.compute(accountId) { _, currentList ->
            val list = currentList ?: CopyOnWriteArrayList()
            list.add(transaction)
            list
        }

        println(compute.toString())
    }


}