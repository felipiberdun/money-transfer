package com.felipiberdun.domain.transaction

import java.util.*

interface TransactionRepository {

    fun createTransaction(transaction: Transaction): Transaction

    fun findByAccountId(accountId: UUID): List<Transaction>

}