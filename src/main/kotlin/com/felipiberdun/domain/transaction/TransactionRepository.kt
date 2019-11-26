package com.felipiberdun.domain.transaction

import io.reactivex.Single
import java.util.*

interface TransactionRepository {

    fun createTransaction(transaction: Transaction): Single<Transaction>

    fun findByAccountId(accountId: UUID): Single<List<Transaction>>

}