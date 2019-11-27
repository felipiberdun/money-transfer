package com.felipiberdun.domain.transaction

import com.felipiberdun.domain.accounts.Account
import com.felipiberdun.domain.accounts.AccountService
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton

@Singleton
class TransactionService(private val transactionRepository: TransactionRepository,
                         private val accountService: AccountService) {

    fun createDeposit(createDepositCommand: CreateDepositCommand): Single<Deposit> {
        if (createDepositCommand.amount < 0) {
            return Single.error(InvalidTransactionAmmountException)
        }

        return accountService.findById(createDepositCommand.to)
                .flatMap {
                    val deposit = Deposit(
                            id = UUID.randomUUID(),
                            to = it,
                            amount = createDepositCommand.amount,
                            date = LocalDateTime.now())
                    transactionRepository.createTransaction(deposit)
                            .cast(Deposit::class.java)
                }
    }

    fun createTransfer(createTransferCommand: CreateTransferCommand): Single<Transaction> {
        if (createTransferCommand.amount < 0) {
            return Single.error(InvalidTransactionAmmountException)
        }

        val accountFrom = accountService.findById(createTransferCommand.from)
        val accountTo = accountService.findById(createTransferCommand.to)

        return Single.zip(accountFrom, accountTo,
                BiFunction<Account, Account, Single<Transaction>> { from, to ->
                    if (createTransferCommand.amount >= 0) {
                        throw InsufficientAmountException(createTransferCommand.from, createTransferCommand.amount)
                    }

                    val transfer = Transfer(
                            id = UUID.randomUUID(),
                            from = from,
                            to = to,
                            amount = createTransferCommand.amount,
                            date = LocalDateTime.now())

                    transactionRepository.createTransaction(transfer)
                })
                .flatMap { it }
    }

    fun createWithdraw(createWithdrawCommand: CreateWithdrawCommand): Single<Transaction> {
        if (createWithdrawCommand.amount < 0) {
            return Single.error(InvalidTransactionAmmountException)
        }

        return accountService.findById(createWithdrawCommand.from)
                .flatMap {
                    if (createWithdrawCommand.amount >= 0) {
                        throw InsufficientAmountException(createWithdrawCommand.from, createWithdrawCommand.amount)
                    }

                    val withdraw = Withdraw(
                            id = UUID.randomUUID(),
                            from = it,
                            amount = createWithdrawCommand.amount,
                            date = LocalDateTime.now())

                    transactionRepository.createTransaction(withdraw)
                }
    }

    fun findByAccountId(accountId: UUID): Single<List<Transaction>> {
        return accountService.findById(accountId)
                .flatMap { transactionRepository.findByAccountId(it.id) }
                .map { it.map { transaction -> transaction } }
    }

    fun findByAccountAndId(accountId: UUID, transactionId: UUID): Maybe<Transaction> {
        return accountService.findById(accountId)
                .flatMapMaybe { transactionRepository.findByAccountAndId(it.id, transactionId) }
    }

}