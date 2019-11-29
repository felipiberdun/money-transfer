package com.felipiberdun.domain.transaction

import java.time.LocalDateTime
import java.util.*

sealed class TransactionQuery(val type: TransactionType) {
    companion object {
        fun fromTransaction(transaction: Transaction): TransactionQuery {
            return when (transaction) {
                is Deposit -> transaction.toQuery()
                is Transfer -> transaction.toQuery()
                is Withdraw -> transaction.toQuery()
            }
        }
    }
}

data class DepositQuery(
        val id: UUID,
        val to: UUID,
        val amount: Float,
        val date: LocalDateTime
) : TransactionQuery(TransactionType.DEPOSIT)

data class TransferQuery(
        val id: UUID,
        val from: UUID,
        val to: UUID,
        val amount: Float,
        val date: LocalDateTime
) : TransactionQuery(TransactionType.TRANSFER)

data class WithdrawQuery(
        val id: UUID,
        val from: UUID,
        val amount: Float,
        val date: LocalDateTime
) : TransactionQuery(TransactionType.WITHDRAW)

fun Deposit.toQuery() = DepositQuery(
        id = this.id,
        to = this.to.id,
        amount = this.amount,
        date = this.date
)

fun Transfer.toQuery() = TransferQuery(
        id = this.id,
        from = this.from.id,
        to = this.to.id,
        amount = this.amount,
        date = this.date
)

fun Withdraw.toQuery() = WithdrawQuery(
        id = this.id,
        from = this.from.id,
        amount = this.amount,
        date = this.date
)
