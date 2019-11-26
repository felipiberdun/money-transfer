package com.felipiberdun.domain.transaction

import com.felipiberdun.domain.accounts.Account
import java.time.LocalDateTime
import java.util.*

object InvalidTransactionAmmountException : Exception()
class AccountNotFoundException(val accountId: UUID) : Exception()
class InsufficientAmountException(val accountId: UUID, val amount: Float) : Exception()


enum class TransactionType {
    DEPOSIT,
    TRANSFER,
    WITHDRAW
}

sealed class Transaction(val type: TransactionType)

data class Deposit(
        val id: UUID,
        val to: Account,
        val amount: Float,
        val date: LocalDateTime
) : Transaction(TransactionType.DEPOSIT)

data class Transfer(
        val id: UUID,
        val from: Account,
        val to: Account,
        val amount: Float,
        val date: LocalDateTime
) : Transaction(TransactionType.TRANSFER)

data class Withdraw(
        val id: UUID,
        val from: Account,
        val amount: Float,
        val date: LocalDateTime
) : Transaction(TransactionType.WITHDRAW)
